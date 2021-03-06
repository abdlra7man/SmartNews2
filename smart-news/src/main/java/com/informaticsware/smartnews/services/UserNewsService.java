package com.informaticsware.smartnews.services;

import com.informaticsware.smartnews.exceptions.UserNewsException;
import com.informaticsware.smartnews.model.dto.SmartNewsDTO;
import com.informaticsware.smartnews.model.entities.News;
import com.informaticsware.smartnews.model.entities.User;
import com.informaticsware.smartnews.model.entities.UserNews;
import com.informaticsware.smartnews.repository.NewsRepository;
import com.informaticsware.smartnews.repository.UserRepository;
import com.informaticsware.smartnews.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Name on 6/06/2017.
 */
@Service
public class UserNewsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NewsRepository newsRepository;

    public News getNews(Long newsId){
        return newsRepository.findOne(newsId);
    }

    public News getNewsByLink(String link){
        List<News> news = newsRepository.findByLink(link);
        return news.isEmpty() ? null : news.get(0);
    }

    public User getUser(Long userId){
        return userRepository.findOne(userId);
    }

    public User getUserByUserName(String userName){
        return userRepository.findByUsername(userName);
    }

    public List<News> searchNewsByTitle(final String keyword){
        return newsRepository.findByTitleContaining(keyword);
    }

    public List<News> getAllNews(){
        return newsRepository.findAll();
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User createUser(User user){
        try {
            return userRepository.saveAndFlush(user);
        } catch (Exception de){
            throw new UserNewsException(de.getMessage());
        }
    }

    public News createNews(News news){
        try {
            news.setContent(HttpUtils.getContentsForURL(news.getLink()));
            return newsRepository.saveAndFlush(news);
        } catch (Exception de){
            throw new UserNewsException(de.getMessage());
        }
    }

    public User upsertUserNews(SmartNewsDTO smartNewsDTO){
        User user = null;
        try {
            user = userRepository.findByUsername(smartNewsDTO.getUserName());
            if(user == null){
                user = new User();
                user.setUsername(smartNewsDTO.getUserName());
                user = createUser(user);
                user.setUserNews(new HashSet<>());
            }
            News news = getNewsByLink(smartNewsDTO.getLink());
            if(news == null){
                news = new News();
                news.setAuthor(smartNewsDTO.getAuthor());
                news.setDescription(smartNewsDTO.getDescription());
                news.setTitle(smartNewsDTO.getTitle());
                news.setLink(smartNewsDTO.getLink());
                if(smartNewsDTO.getNewsPublicationTimeStamp() != null){
                    news.setPublishDate(new Date(smartNewsDTO.getNewsPublicationTimeStamp()));
                }
                news = createNews(news);
            }
            UserNews userNews = new UserNews();
            userNews.setAction(smartNewsDTO.getAction());
            userNews.setActionDate(new Date(smartNewsDTO.getActionTimeStamp()));
            userNews.setNews(news);
            userNews.setUser(user);
            user.getUserNews().add(userNews);
            user = userRepository.saveAndFlush(user);
        } catch (Exception de){
            throw new UserNewsException(de.getMessage());
        }
        return user;
    }
}
