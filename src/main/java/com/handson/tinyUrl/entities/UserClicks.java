package com.handson.tinyUrl.entities;


import com.datastax.oss.driver.api.core.cql.Row;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class UserClicks {
    static DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private String name;
    private Date time;
    private String tiny;
    private String url;

    @Override
    public String toString() {
        return "UserClicks{" +
                "name='" + name + '\'' +
                ", time=" + time +
                ", tiny='" + tiny + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public Date getTime() {
        return time;
    }

    public String getTiny() {
        return tiny;
    }

    public String getUrl() {
        return url;
    }

    private static String trimQuotes(String string){
        return string.substring(
                string.indexOf("'") + 1,
                string.indexOf("'", 1));
    }

    public static UserClicks parse(Row userClicks){
        if(userClicks != null) {
            try {
                String[] columns = userClicks.getFormattedContents().split(",");
               return  UserClicksBuilder.anUserClicks().time(extractTime(columns)).url(extractUrl(columns))
                        .name(extractName(columns)).tiny(extractTiny(columns)).build();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }else {
            return null;
        }
    }

    private static String extractName(String[] columns) {
        return trimQuotes(extractValue(columns, "user_name"));
    }

    private static Date extractTime(String[] columns) {
        String noZone = trimQuotes(extractValue(columns, "click_time"));
        noZone = noZone.substring(noZone.indexOf('+')+1);
        try {
            return df1.parse(noZone);
        }catch (Exception e) {
            return new Date();
        }
    }

    private static String extractTiny(String[] columns) {
        return extractValue(columns, "click_tiny");
    }

    private static String extractUrl(String[] columns) {
        return trimQuotes(extractValue(columns, "click_url"));
    }

    private static String extractValue(String[] columns, String key){
        String result = null;
        for (String column : columns) {
            if (column.contains(key)) {
                result = column.substring(column.indexOf(":") +1);// split(":")[1].trim();
                break;
            }
        }

        return result;
    }

    public static final class UserClicksBuilder {
        private String name;
        private Date time;
        private String tiny;
        private String url;

        private UserClicksBuilder() {
        }

        public static UserClicksBuilder anUserClicks() {
            return new UserClicksBuilder();
        }

        public UserClicksBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserClicksBuilder time(Date time) {
            this.time = time;
            return this;
        }

        public UserClicksBuilder tiny(String tiny) {
            this.tiny = tiny;
            return this;
        }

        public UserClicksBuilder url(String url) {
            this.url = url;
            return this;
        }

        public UserClicks build() {
            UserClicks userClicks = new UserClicks();
            userClicks.url = this.url;
            userClicks.name = this.name;
            userClicks.tiny = this.tiny;
            userClicks.time = this.time;
            return userClicks;
        }
    }
}
