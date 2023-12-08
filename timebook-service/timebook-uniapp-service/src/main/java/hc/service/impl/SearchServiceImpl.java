package hc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.Sensitive;
import hc.service.AlbumService;
import hc.service.ImageNoteService;
import hc.service.ImagesService;
import hc.service.SearchService;
import hc.common.dtos.ResponseResult;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.uniapp.customize.SearchDto;
import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.dtos.ElasticSearchNoteDto;
import hc.uniapp.note.dtos.ImageNoteDto;
import hc.uniapp.note.pojos.Note;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static hc.common.constants.ElasticSearchConstants.*;
import static hc.common.enums.AppHttpCodeEnum.SUCCESS;

@Service
public class SearchServiceImpl implements SearchService {
    @Resource
    private ImagesService imagesService;
    @Resource
    private AlbumService albumService;
    @Resource
    private ISensitiveClient sensitiveClient;
    @Resource
    private ElasticSearcherClient elasticSearcherClient;
    @Resource
    private ImageNoteService imageNoteService;
    @Override
    public ResponseResult searchAll(String content) {
        Set<Image> images=new HashSet<>();
        Set<Album> albums=new HashSet<>();
        List<String> split =Arrays.stream(content.split("\\s+")).collect(Collectors.toList());
        Sensitive sensitive=new Sensitive();
        ResponseResult result=new ResponseResult<>();
        Set<String> setSplit=new HashSet<>(split);
        for (String str : setSplit) {
            result=sensitiveClient.checkIsSensitive(sensitive.setSensitives(str));
            if(result.getCode()!=SUCCESS.getCode())
                return result;
            if(str.contains("-")&&checkDateTime(str)){
                str+=" 00:00:00";
                String Tomorrow = processTime(str);
                if(!StrUtil.isBlank(Tomorrow)) {
                    List<Image> list = imagesService.query()
                            .eq("user_id", UserHolder.getUser().getUserId())
                            .ge("create_time", str)
                            .le("create_time", Tomorrow).list();
                    images.addAll(list);
                }
            }else{
                List<Album> list = albumService.query().eq("user_id",UserHolder.getUser().getUserId())
                        .like("album_name", str).list();
                albums.addAll(list);
            }
        }
        List<Image> imageList=imageSortByDesc(images);
        List<Album> albumList=albumSortByDesc(albums);
        SearchDto searchDto=new SearchDto().setAlbumList(albumList).setImageList(imageList);
        return ResponseResult.okResult(searchDto);
    }

    @Override
    public ResponseResult searchNote(String content) {
        Sensitive sensitive=new Sensitive().setSensitives(content);
        ResponseResult result = sensitiveClient.checkIsSensitive(sensitive);
        if(result.getCode()!=SUCCESS.getCode())
            return result;
        List<ElasticSearchNoteDto> noteDtos = elasticSearcherClient
                .queryHighLightAndSort(TIME_BOOK, CONTENT, content, CREATE_TIME, CONTENT);
        List<Note> noteList=new ArrayList<>();
        for(ElasticSearchNoteDto elasticSearchNoteDto:noteDtos) {
            Note note=new Note();
            BeanUtil.copyProperties(elasticSearchNoteDto,note);
            List<ImageNoteDto> imageNoteDtoList = imageNoteService.query()
                    .eq("note_id", note.getNoteId()).list();
            List<Image> images=new ArrayList<>();
            for (ImageNoteDto i: imageNoteDtoList){
                Image image = imagesService.getById(i.getImageId());
                images.add(image);
            }
            note.setImages(images);
            noteList.add(note);
        }
        return ResponseResult.okResult(noteList);
    }
    private boolean checkDateTime(String time){
        try {
            LocalDate.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    private String processTime(String time)  {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            Date date = dateFormat.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            return "";
        }
    }
    private List<Image> imageSortByDesc(Set<Image> set){
        if(CollUtil.isEmpty(set))
            return null;
        List<Image> list=new ArrayList<>();
        list.addAll(set);
        Collections.sort(list, new Comparator<Image>(){
            @Override
            public int compare(Image i1, Image i2) {
                return i2.getCreateTime().compareTo(i1.getCreateTime());
            }
        });
        return list;
    }
    private List<Album> albumSortByDesc(Set<Album> set){
        if(CollUtil.isEmpty(set))
            return null;
        List<Album> list=new ArrayList<>();
        list.addAll(set);
        Album defaultAlbum=null;
        int removeId=0;
        Collections.sort(list, new Comparator<Album>() {
            @Override
            public int compare(Album a1, Album a2) {
                return a2.getCreateTime().compareTo(a1.getCreateTime());
            }
        });
        for(Album album:list){
            if(album.getType()==0) {
                defaultAlbum = album;
                removeId=list.indexOf(album);
            }
        }
        if(defaultAlbum!=null) {
            list.remove(removeId);
            list.add(0, defaultAlbum);
        }
        return list;
    }
}