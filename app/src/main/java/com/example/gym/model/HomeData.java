package com.example.gym.model;

public class HomeData {

    public final double weight;
    public final String bodyPart;
    public final double dailyCalories;
    public final boolean restDay;
    public final int cycleCount;
    public final boolean trainedToday;
    public final double todayCalories;

    public HomeData(double weight, String bodyPart, double dailyCalories, boolean restDay,
                    int cycleCount, boolean trainedToday, double todayCalories) {
        this.weight = weight;
        this.bodyPart = bodyPart;
        this.dailyCalories = dailyCalories;
        this.restDay = restDay;
        this.cycleCount = cycleCount;
        this.trainedToday = trainedToday;
        this.todayCalories = todayCalories;
    }
}
