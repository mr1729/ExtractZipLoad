package com.example.sai.ezl.component;

import com.example.sai.ezl.batch.Zip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EzlItemWriter implements ItemWriter<Zip> {
    @Override
    public void write(List<? extends Zip> items) throws Exception {
    }
}