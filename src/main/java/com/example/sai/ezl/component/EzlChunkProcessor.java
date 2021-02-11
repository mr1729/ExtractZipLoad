package com.example.sai.ezl.component;


import com.example.sai.ezl.batch.Zip;
import lombok.SneakyThrows;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.item.Chunk;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EzlChunkProcessor extends SimpleChunkProcessor<Zip, Zip> {

    public EzlChunkProcessor(ItemWriter<? super Zip> ezlItemWriter) {
        super(ezlItemWriter);
    }

    @Override
    public Chunk<Zip> transform(StepContribution contribution, Chunk<Zip> inputs) throws Exception {
        return new Chunk<>(inputs.getItems().stream().map(this::processItem).collect(Collectors.toList()));
    }


    @SneakyThrows
    private Zip processItem(Zip item)   {
        return doProcess(item);
    }
}
