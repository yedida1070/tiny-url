package com.handson.tinyUrl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.tinyUrl.entities.User;
import com.handson.tinyUrl.entities.UserClicks;
import com.handson.tinyUrl.repository.UserRepository;
import com.handson.tinyUrl.service.Cassandra;
import com.handson.tinyUrl.service.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.handson.tinyUrl.entities.User.UserBuilder.anUser;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@RestController
@RequestMapping(value = "")
public class AppController {
    private static final int TINY_LENGTH = 6;
    public static final int MAX_RETRIES = 10;
    @Autowired
    Redis redis;
    private Random random = new Random();
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Cassandra cassandra;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/tiny/{tiny}/clicks", method = RequestMethod.GET)
    public List<UserClicks> getClicks(@PathVariable String tiny) throws JsonProcessingException {
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = objectMapper.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        System.out.println("longUrl is: " + tinyRequest.getLongUrl());
        return cassandra.getUserClicks(tinyRequest.getUserId());
    }

    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
    public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyUrl = generateTinyUrl();
        String userId = request.getUserId();
        int i = 0;
        while (!redis.set(tinyUrl, objectMapper.writeValueAsString(request)) && i < MAX_RETRIES) {
            tinyUrl = generateTinyUrl();
            if (userId != null) {
                Query query = Query.query(Criteria.where("_id").is(userId));
                Update update = new Update().set("shorts."  + tinyUrl, new HashMap() );
                mongoTemplate.updateFirst(query, update, "users");
            }
            i++;
        }
        return "https://tiny-yema.herokuapp.com/" + tinyUrl + "/";
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        System.out.println("getRequest for tiny: " + tiny);
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = objectMapper.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        System.out.println("longUrl is: " + tinyRequest.getLongUrl());
        if (tinyRequest.getLongUrl() != null) {
            String userId = tinyRequest.getUserId();
            if ( userId != null) {
                incrementMongoField(userId, "allUrlClicks");
                incrementMongoField(userId,
                        "shorts."  + tiny + ".clicks." + getCurMonth());
                cassandra.insertUserClicks(userId,tiny,tinyRequest.getLongUrl());
            }

            return new ModelAndView("redirect:" + tinyRequest.getLongUrl().toString());
        } else {
            throw new RuntimeException(tiny + " not found");
        }
    }
    private String getCurMonth() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM");
        Date date = new Date();

        return formatter.format(date);
    }

    @RequestMapping(value = "/newUser", method = RequestMethod.POST)
    public String createUser(@RequestParam String id, @RequestParam String name) {
        User user = anUser().withId(id).withName(name).build();
        mongoTemplate.insert(user,"users");

        return "OK";
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.PUT)
    public String updateUser(@RequestBody User user) {
        userRepository.save(user);
        return "OK";
    }

    private String generateTinyUrl() {
        String charPool = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TINY_LENGTH; i++) {
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return res.toString();
    }
    private void incrementMongoField(String id, String key){
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update().inc(key, 1);
        mongoTemplate.updateFirst(query, update, "users");
    }


}
