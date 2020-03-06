package plywacz.openx.service;
/*
Author: BeGieU
Date: 05.03.2020
*/

import plywacz.openx.model.Post;
import plywacz.openx.model.User;
import plywacz.openx.model.UserPostContainer;

import java.util.*;

public class DataManipulatorImpl implements DataManipulator {

    //maximum integer value that can be represented by Double
    public static final double EARTH_CIRCUIT = 40075;

    @Override
    public Set<UserPostContainer> joinData(Set<User> users, Set<Post> posts) {
        Set<UserPostContainer> userPostContainers = new HashSet<>();
        users.forEach(user -> userPostContainers.add(new UserPostContainer(user)));

        posts.forEach(post -> {
            var postOwner = findUser(userPostContainers, post.getUserId());
            if (postOwner == null) {//todo if find user return null it means downloaded data is faulty
                throw new RuntimeException("given data is faulty. Post: " + post + " has no owner !!!");
            }
            postOwner.addPost(post);
        });
        return userPostContainers;
    }

    private UserPostContainer findUser(Set<UserPostContainer> userPostContainers, Long userId) {
        for (var user : userPostContainers) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }


    @Override
    public List<String> countPosts(Set<UserPostContainer> users) {
        var stringList = new LinkedList<String>();
        users.forEach(user -> stringList.add(user.getPostCountString()));

        return stringList;
    }


    @Override public List<String> findDuplicateTitles(Set<Post> posts) {
        var duplicateChecker = new HashSet<String>();
        var duplicateList = new LinkedList<String>();

        posts.forEach(post -> {
            var currentTitle = post.getTitle();
            if (!duplicateChecker.add(currentTitle)) {
                duplicateList.add(currentTitle);
            }
        });
        return duplicateList;
    }

    @Override
    public Map<User, User> findClosestUser(Set<User> users) {
        // O(n^2) loop because in some cases for user1, user2 is closest and
        //for user2, user3 is closest
        var closestUserMap = new HashMap<User, User>();
        for (var user1 : users) {
            User closestUser = null;
            double minDist = EARTH_CIRCUIT;
            for (var user2 : users) {
                if (!user1.equals(user2) && calculateDistanceBetweenUsers(user1, user2) < minDist) {
                    minDist = calculateDistanceBetweenUsers(user1, user2);
                    closestUser = user2;
                }
            }
            closestUserMap.put(user1, closestUser);
        }

        return closestUserMap;
    }

    /**
     * implementation of Haversine formula,
     * ref: https://en.wikipedia.org/wiki/Haversine_formula
     */
    private Double calculateDistanceBetweenUsers(User user1,
                                                 User user2) {
        var lon1 = Double.parseDouble(user1.getAddress().getGeo().getLng());
        var lat1 = Double.parseDouble(user1.getAddress().getGeo().getLat());
        var lon2 = Double.parseDouble(user2.getAddress().getGeo().getLng());
        var lat2 = Double.parseDouble(user2.getAddress().getGeo().getLat());

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        var dlon = lon2 - lon1;
        var dlat = lat2 - lat1;

        var a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        var c = 2 * Math.asin(Math.sqrt(a));

        // calculate the result
        return (c * EARTH_RADIUS_KM);
    }

    public static void main(String[] args) {
        var downloader = new DataDownloaderImpl();
        var dm = new DataManipulatorImpl();

        var userPostMap = dm.joinData(downloader.fetchUserData(), downloader.fetchPostData());
        dm.countPosts(userPostMap);
        dm.findDuplicateTitles(downloader.fetchPostData());
        dm.findClosestUser(downloader.fetchUserData());

    }
}


