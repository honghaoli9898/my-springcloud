package com.seaboxdata.sdps.user.task.controller;

import com.seaboxdata.sdps.user.task.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class DownloadController {
    @GetMapping("/download/keytab")
    public void  download(
            @RequestParam String username, @RequestParam String keytab_file_name,
             HttpServletRequest request, HttpServletResponse response)
        throws  Exception
    {
        String fileName = "file_" + System.currentTimeMillis();
        String downloadPath = "/data/SDP7.1/sdps/sdp7.1/keytab/usersync/" + fileName;
        String downloadName = fileName;
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Dijdssition","attachment;fileName=" + FileUtils.setFileDownloadHeader(request, downloadName));
        FileUtils.writeBytes(downloadPath, response.getOutputStream());

    }

}