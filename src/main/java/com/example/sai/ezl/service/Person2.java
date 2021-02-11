package com.example.sai.ezl.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("person2")
public class Person2  implements PersonInterface{
    @Override
    public String getName() {
        return ">======Person2---->";
    }

}
