package hc.service.impl;

import cn.hutool.core.util.StrUtil;
import hc.service.AlbumService;
import hc.service.ImagesService;
import hc.service.SearchService;
import hc.common.dtos.ResponseResult;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.uniapp.customize.SearchDto;
import hc.uniapp.image.pojos.Image;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static hc.LogUtils.error;
import static hc.LogUtils.info;

@Service
public class SearchServiceImpl implements SearchService {
    @Resource
    private ImagesService imagesService;
    @Resource
    private AlbumService albumService;


    @Override
    public ResponseResult searchAll(String context) {
        List<Image> imageList=new ArrayList<>();
        List<Album> albumList=new ArrayList<>();
        List<String> split = StrUtil.split(context, ' ');
        Set<String> setSplit=new HashSet<>(split);
        for (String str : setSplit) {
            if(str.contains("-")){
                str+=" 00:00:00";
                String Tomorrow = processTime(str);
                if(!StrUtil.isBlank(Tomorrow)) {
                    List<Image> list = imagesService.query()
                            .eq("user_id", UserHolder.getUser().getUserId())
                            .ge("create_time", str)
                            .le("create_time", Tomorrow).list();
                    imageList.addAll(list);
                }
            }else{
                List<Album> list = albumService.query().eq("user_id",UserHolder.getUser().getUserId())
                        .like("album_name", str).list();
                albumList.addAll(list);
            }
        }
        imageList=imageSortByDesc(imageList);
        albumList=albumSortByDesc(albumList);
        SearchDto searchDto=new SearchDto().setAlbumList(albumList).setImageList(imageList);
        return ResponseResult.okResult(searchDto);
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
    private List<Image> imageSortByDesc(List<Image> list){
        Collections.sort(list, new Comparator<Image>(){
            @Override
            public int compare(Image i1, Image i2) {
                return i2.getCreateTime().compareTo(i1.getCreateTime());
            }
        });
        return list;
    }
    private List<Album> albumSortByDesc(List<Album> list){
        Collections.sort(list, new Comparator<Album>(){
            @Override
            public int compare(Album a1, Album a2) {
                return a2.getCreateTime().compareTo(a1.getCreateTime());
            }
        });
        return list;
    }
}
