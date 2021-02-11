package com.example.sai.ezl;

import com.example.sai.ezl.service.EZLJobLauncherService;
import com.example.sai.ezl.service.SshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("master")
@RestController
public class EZLcontroller {
    @Autowired
    EZLJobLauncherService ezlJobLauncherService;

    @Autowired
    SshService sshService;

    @GetMapping("/start")
    public String BacthProcess(@RequestParam("zip") String zip){
        ezlJobLauncherService.launch(zip);
        return "Batch Strarted";
    }
}

