package com.cheftory.api.ranking;

public enum RankingSurfaceType {
    CUISINE_KOREAN,
    CUISINE_SNACK,
    CUISINE_CHINESE,
    CUISINE_JAPANESE,
    CUISINE_WESTERN,
    CUISINE_DESSERT,
    CUISINE_HEALTHY,
    CUISINE_BABY,
    CUISINE_SIMPLE;

    private static final String MESSAGE_KEY_PREFIX = "recipe.cuisine.";
    private static final String CUISINE_PREFIX = "CUISINE_";

    public String messageKey() {
        String name = name();
        String suffix = name.startsWith(CUISINE_PREFIX) ? name.substring(CUISINE_PREFIX.length()) : name;
        return MESSAGE_KEY_PREFIX + suffix.toLowerCase();
    }
}