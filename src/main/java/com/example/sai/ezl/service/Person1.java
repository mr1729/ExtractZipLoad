package com.example.sai.ezl.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("person1")
public class Person1 implements PersonInterface {
    @Override
    public String getName() {
        return "Person1";
    }
}
