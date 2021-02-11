package com.example.sai.ezl.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class SshService {

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
    private String password;

    @Value("${ssh.port}")
    private int port;

    @Value("${ssh.host}")
    private String host;

    private final JSch sshTunnel;

    public SshService(){
         sshTunnel = new JSch();
    }

    public String sendCommand(String command){

        String response ="No Response";
        try {
            Session session = sshTunnel.getSession(username,host);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking","no");
            session.connect();
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channelExec.setOutputStream(responseStream);
            channelExec.connect();
            while (channelExec.isConnected()) {
                Thread.sleep(100);
            }
            response = new String(responseStream.toByteArray());
            System.out.println(response);
            channelExec.disconnect();
        } catch (JSchException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

}
