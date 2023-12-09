package hc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.RedisCacheClient;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import hc.common.exception.CustomizeException;
import hc.common.exception.ParamErrorException;
import hc.mapper.NoteMapper;
import hc.service.ImageNoteService;
import hc.service.ImagesService;
import hc.service.NoteService;
import hc.thread.UserHolder;
import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.dtos.ImageNoteDto;
import hc.uniapp.note.dtos.NoteHighDocDto;
import hc.uniapp.note.dtos.NoteDto;
import hc.uniapp.note.dtos.NoteSugDocDto;
import hc.uniapp.note.pojos.Note;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static hc.LogUtils.info;
import static hc.common.constants.ElasticSearchConstants.TIME_BOOK;
import static hc.common.constants.RedisConstants.*;
import static hc.common.enums.AppHttpCodeEnum.*;


@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
    @Resource
    private ImagesService imagesService;
    @Resource
    private ImageNoteService imageNoteService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisCacheClient redisCacheClient;
    @Resource
    private ISensitiveClient sensitiveClient;
    @Resource
    private ElasticSearcherClient elasticSearcherClient;
    @Override
    public ResponseResult findList() {
        List<Note> notes=query().eq("user_id", UserHolder.getUser().getUserId())
                .orderByDesc("create_time").list().stream().peek(note -> {
                    List<ImageNoteDto> imageNoteList=imageNoteService.query().eq("note_id",note.getNoteId()).list();
                    List<Image> images=new ArrayList<>();
                    for (ImageNoteDto imageNoteDto:imageNoteList){
                        Image image = redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,imageNoteDto.getImageId(),Image.class,
                                imagesService::getById,CACHE_IMAGE_TTL, TimeUnit.DAYS);
                        images.add(image);
                    }
                    note.setImages(images);
                }).collect(Collectors.toList());
        return ResponseResult.okResult(notes);
    }

    @Override
    @Transactional
    public ResponseResult updateNote(NoteDto noteDto) {
        if(StrUtil.isBlank(noteDto.getNoteId())||(StrUtil.isBlank(noteDto.getContent())
                &&StrUtil.isBlank(noteDto.getEmoji())&& CollUtil.isEmpty(noteDto.getImageIds()))){
            info("无法修改日记");
            throw new ParamErrorException(AppHttpCodeEnum.NO_FOUND_PARAM);
        }
        Note org=redisCacheClient.queryWithPassThrough(CACHE_NOTE_KEY, noteDto.getNoteId(),Note.class,
                this::getById,CACHE_NOTE_TTL,TimeUnit.DAYS);
        if(org==null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"未找到该笔记");
        if(StrUtil.isNotBlank(noteDto.getContent())) {
            Sensitive sensitive=new Sensitive().setSensitives(noteDto.getContent());
            ResponseResult result = sensitiveClient.checkIsSensitive(sensitive);
            if(result.getCode()!=SUCCESS.getCode())
                return result;
            org.setContent(noteDto.getContent());
        }
        if(StrUtil.isNotBlank(noteDto.getEmoji())) {
            org.setEmoji(noteDto.getEmoji());
        }
        if(CollUtil.isNotEmpty(noteDto.getImageIds())) {
            List<Image> imageList = new ArrayList<>();
            List<String> imageIds = noteDto.getImageIds();
            for (String id : imageIds) {
                Image image = redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY, id, Image.class,
                        imagesService::getById, CACHE_IMAGE_TTL, TimeUnit.DAYS);
                imageList.add(image);
            }
            org.setImages(imageList);
        }
        stringRedisTemplate.delete(CACHE_NOTE_KEY+noteDto.getNoteId());
        elasticSearcherClient.deleteDocument(TIME_BOOK,org.getNoteId());
        updateById(org);
        elasticSearcherClient.addIndexDocumentById(TIME_BOOK,
                new NoteHighDocDto(org), NoteHighDocDto::getNoteId);
        return ResponseResult.okResult(200,"修改成功");
    }

    @Override
    @Transactional
    public ResponseResult saveNote(NoteDto noteDto) {
        if(StrUtil.isBlank(noteDto.getContent())
                ||StrUtil.isBlank(noteDto.getEmoji())||CollUtil.isEmpty(noteDto.getImageIds())){
            info("无法新增日记");
            throw new ParamErrorException(AppHttpCodeEnum.NO_FOUND_PARAM);
        }
        Sensitive sensitive=new Sensitive().setSensitives(noteDto.getContent());
        ResponseResult result = sensitiveClient.checkIsSensitive(sensitive);
        if(result.getCode()!=SUCCESS.getCode())
            return result;
        Note note=new Note().setUserId(UserHolder.getUser().getUserId());
        save(note);
        note.setContent(noteDto.getContent())
                .setEmoji(noteDto.getEmoji());
        updateById(note);
        for(String id: noteDto.getImageIds()) {
            ImageNoteDto imageNoteDto = new ImageNoteDto().setNoteId(note.getNoteId())
                    .setImageId(id);
            imageNoteService.save(imageNoteDto);
        }
        boolean bool = elasticSearcherClient.addIndexDocumentById(TIME_BOOK,
                new NoteSugDocDto(note), NoteSugDocDto::getNoteId);
        if(!bool){
            throw new CustomizeException("es新增搜索失败");
        }
        return ResponseResult.okResult();
    }

    @Override
    @Transactional
    public ResponseResult deleteNote(String noteId) {
        removeById(noteId);
        List<String> removeImageNoteIds = imageNoteService.query().eq("note_id", noteId).list().stream().map(ImageNoteDto::getImageNoteId).collect(Collectors.toList());
        imageNoteService.removeByIds(removeImageNoteIds);
        boolean bool = elasticSearcherClient.deleteDocument(TIME_BOOK, noteId);
        if(!bool){
            throw new CustomizeException("es删除搜索失败");
        }
        return ResponseResult.okResult();
    }
}
