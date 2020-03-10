package com.atguigu.gmall.user.controller;

import java.util.Random;

public class RandomPeople {

    public static void main(String[] args) {
        Random col = new Random();
        Random row = new Random();

        int lie = col.nextInt(4)+1;

        int hang = row.nextInt(8)+1;

        System.out.print("有请第"+lie+"列"+"第"+hang+"行"+"的同学回答问题。。。");

    }
}
