package com.handson.tinyUrl.entities;


import com.datastax.oss.driver.api.core.cql.Row;

import java.util.Date;
import java.util.Objects;

public class UserClicks {
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
        return trimQuotes(extractValue(columns, "name"));
    }

    private static Date extractTime(String[] columns) {
        return new Date(Long.valueOf(trimQuotes(extractValue(columns, "time"))));
    }

    private static String extractTiny(String[] columns) {
        return extractValue(columns, "tiny");
    }

    private static String extractUrl(String[] columns) {
        return trimQuotes(extractValue(columns, "url"));
    }

    private static String extractValue(String[] columns, String key){
        String result = null;
        for (String column : columns) {
            if (column.contains(key)) {
                result = column.split(":")[1].trim();
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
