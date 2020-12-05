package com.streever.hadoop.hdfs.util;

import com.streever.hadoop.hdfs.shell.command.Constants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
Used to track the current state of available and accessed namespaces during the session.

 */
public class FileSystemOrganizer {

    private Map<String, FileSystemState> namespaces = new TreeMap<String, FileSystemState>();
    private FileSystemState defaultFileSystemState = null;
    private FileSystemState currentFileSystemState = null;
//    private FileSystem distributedFileSystem = null;
//    private FileSystem localFileSystem = null;

    private Configuration config = null;
    private FileSystem distributedFileSystem = null;
    private FileSystem localFileSystem = null;

    public FileSystemOrganizer(Configuration config) {
        this.config = config;
        init();
    }

    public FileSystem getDistributedFileSystem() {
        return distributedFileSystem;
    }

    public void setDistributedFileSystem(FileSystem distributedFileSystem) {
        this.distributedFileSystem = distributedFileSystem;
    }

    public Map<String, FileSystemState> getNamespaces() {
        return namespaces;
    }

    public FileSystem getLocalFileSystem() {
        return localFileSystem;
    }

    public void setLocalFileSystem(FileSystem localFileSystem) {
        this.localFileSystem = localFileSystem;
    }

    public FileSystemState getDefaultFileSystemState() {
        return defaultFileSystemState;
    }

    public void setDefaultFileSystemState(FileSystemState defaultFileSystemState) {
        this.defaultFileSystemState = defaultFileSystemState;
    }

    public FileSystemState getCurrentFileSystemState() {
        return currentFileSystemState;
    }

    public void setCurrentFileSystemState(FileSystemState currentFileSystemState) {
        this.currentFileSystemState = currentFileSystemState;
    }

    public FileSystemState getFileSystemState(String uri) {
        // Extract the namespace from the uri

        // Check for it in the existing namespaces.
        // If Available, get it and return

        // if not:  create and set
        //  NEED to see if command was successful before adding?

        return namespaces.get(uri);
    }

    protected void init() {
        // Get the defaultFS.
        try {
            distributedFileSystem = FileSystem.get(config);
            localFileSystem = FileSystem.getLocal(config);
            String defaultFS = config.get("fs.defaultFS");
            String configuredNameservices = config.get("dfs.nameservices");
            String[] nameservices = configuredNameservices.split(",");
            for (String nameservice : nameservices) {
                if (defaultFS.contains(nameservice)) {
                    // Setup Default State
                    FileSystemState fss = new FileSystemState();
                    fss.setFileSystem(distributedFileSystem);
                    fss.setNamespace(nameservice);
                    fss.setProtocol("hdfs://");
                    fss.setWorkingDirectory(new Path("/"));
                    namespaces.put(nameservice, fss);
                    defaultFileSystemState = fss;
                    currentFileSystemState = fss;
                } else {
                    FileSystemState fss = new FileSystemState();
                    fss.setFileSystem(distributedFileSystem);
                    fss.setNamespace(nameservice);
                    fss.setProtocol("hdfs://");
                    fss.setWorkingDirectory(new Path("/"));
                    namespaces.put(nameservice, fss);
                }
            }
            // Build the Local FileSystemState
            FileSystemState lfss = new FileSystemState();
            lfss.setFileSystem(localFileSystem);
//            lfss.setNamespace("LOCAL");
            lfss.setProtocol("file:");
            lfss.setWorkingDirectory(localFileSystem.getWorkingDirectory());
            namespaces.put(Constants.LOCAL_FS, lfss);
        } catch (IOException ioe) {
            //
            ioe.printStackTrace();
        }
    }

    public boolean isCurrentLocal() {
        if (getCurrentFileSystemState().equals(getFileSystemState(Constants.LOCAL_FS))) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public String getPrompt() {
        StringBuilder sb = new StringBuilder();
        Set<String> lclNss = namespaces.keySet();
        for (String namespace : lclNss) {
            FileSystemState lclFss = namespaces.get(namespace);
            if (lclFss.equals(currentFileSystemState)) {
                sb.append("*");
            }
            if (lclFss.equals(defaultFileSystemState)) {
                sb.append("+");
            }
            sb.append("\t");
            sb.append(namespace).append(":");
            sb.append(lclFss.toDisplay());
            sb.append("\n");
        }
        sb.append("$:");
        return sb.toString();
    }
}
