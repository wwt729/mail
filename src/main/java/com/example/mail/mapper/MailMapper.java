package com.example.mail.mapper;

import com.example.mail.dto.MailDTO;
import com.example.mail.entity.Mail;
import com.example.mail.utils.CommonMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MailMapper extends CommonMapper<Mail> {
    List<MailDTO> selectByTime(@Param("endTime") String endTime);
}
