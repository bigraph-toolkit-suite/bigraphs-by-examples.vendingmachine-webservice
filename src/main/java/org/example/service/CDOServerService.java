package org.example.service;

import org.bigraphs.spring.data.cdo.CDOStandaloneServer;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * A Spring service class to start and stop the CDO server.
 *
 * @author Dominik Grzelak
 */
@Service
@Scope("singleton")
public class CDOServerService {

    @Autowired
    private ApplicationContext ctx;

    public CDOStandaloneServer server;
    boolean started = false;
    boolean isStarting = false;

    private Future<Void> startServer() {
        isStarting = true;
        try {
            if (server == null) {
                InputStream resourceAsStream = ResourceLoader.getResourceStream("config/cdo-server.xml");
//                assert resourceAsStream != null;
                if (resourceAsStream == null) {
                    throw new IllegalStateException("CDO configuration file missing: " + "config/cdo-server.xml");
                }
                File tempFile = File.createTempFile("cdo-server", ".xml");
                FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
                server = new CDOStandaloneServer(tempFile);
            }
            System.out.println("Server starting now ...");
            started = true;
            CDOStandaloneServer.start(server);
        } catch (Exception e) {
            e.printStackTrace();
            started = false;
            isStarting = false;
            int exitCode = SpringApplication.exit(ctx);
            System.exit(exitCode);
        }
        isStarting = false;
        return new AsyncResult<Void>(null);
    }

    @Async//("taskExecutor2")
    public synchronized void tryStartingCdoServer() {
        if (!this.started() && !this.isStarting()) {
            this.startServer();
        }
    }

    public boolean isStarting() {
        return isStarting;
    }

    public boolean started() {
        return started;
    }

    public synchronized void stopServer() {
        if (server != null)
            server.stop();
    }

}
