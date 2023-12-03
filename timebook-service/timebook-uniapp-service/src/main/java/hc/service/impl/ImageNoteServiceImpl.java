package hc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.mapper.ImageNoteMapper;
import hc.mapper.ImagesMapper;
import hc.service.ImageNoteService;
import hc.uniapp.note.dtos.ImageNoteDto;
import org.springframework.stereotype.Service;

@Service
public class ImageNoteServiceImpl extends ServiceImpl<ImageNoteMapper, ImageNoteDto> implements ImageNoteService {
}
