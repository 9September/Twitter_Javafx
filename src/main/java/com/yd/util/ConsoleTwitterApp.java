package com.yd.util;

import com.yd.dao.FollowDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.UserDAO;
import com.yd.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleTwitterApp {

    private static UserDAO userDAO = new UserDAO();
    private static PostDAO postDAO = new PostDAO();
    private static FollowDAO followDAO = new FollowDAO();

    private static User currentUser = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (currentUser == null) {
                System.out.println("Input zero to log in, one to sign up");
                String choice = scanner.nextLine();
                if (choice.equals("0")) {
                    login(scanner);
                } else if (choice.equals("1")) {
                    signUp(scanner);
                } else {
                    System.out.println("Invalid choice. Please try again!");
                }
            } else {
                // 로그인된 상태
                System.out.println("0 to write post, 1 to write comment, 3 to like post, 4 to like comment, 5 to see my followers, 6 to see my following, 7 to follow someone");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "0":
                        writePost(scanner);
                        break;
                    case "1":
                        // writeComment(scanner);
                        System.out.println("Write comment functionality not implemented yet.");
                        break;
                    case "3":
                        likePost(scanner);
                        break;
                    case "4":
                        // likeComment(scanner);
                        System.out.println("Like comment functionality not implemented yet.");
                        break;
                    case "5":
                        seeFollowers();
                        break;
                    case "6":
                        seeFollowing();
                        break;
                    case "7":
                        followUser(scanner);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again!");
                }
            }
        }
    }

    private static void login(Scanner scanner) {
        System.out.println("Input userid / password");
        String userid = scanner.nextLine();
        String password = scanner.nextLine();

        User user = userDAO.loginUser(userid, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Logged in!!");
        } else {
            System.out.println("wrong id/password. Please log in again.");
        }
    }

    private static void signUp(Scanner scanner) {
        System.out.println("Input userid / password");
        String userid = scanner.nextLine();
        String password = scanner.nextLine();

        // 추가 정보 입력 (선택사항)
        System.out.println("Input email:");
        String email = scanner.nextLine();
        System.out.println("Input birthday (YYYY-MM-DD):");
        String birthdayStr = scanner.nextLine();
        System.out.println("Input phone number:");
        String phone = scanner.nextLine();

        LocalDate birthday;
        try {
            birthday = LocalDate.parse(birthdayStr);
        } catch (Exception e) {
            System.out.println("Invalid birthday format. Using default date 1990-01-01.");
            birthday = LocalDate.of(1990, 1, 1);
        }

        User user = new User(userid, password, email, birthday, phone);
        boolean success = userDAO.registerUser(user);
        if (success) {
            System.out.println("User registered successfully!");
        } else {
            System.out.println("User name already exists. Please try again!");
        }
    }

    private static void writePost(Scanner scanner) {
        System.out.println("Enter post text:");
        String text = scanner.nextLine();
        boolean success = postDAO.addPost(text, null, currentUser.getId());
        if (success) {
            // 마지막으로 추가된 포스트의 ID 가져오기
            int postId = postDAO.getLastInsertId();
            System.out.println("Post created with ID: p" + postId);
        } else {
            System.out.println("Failed to add post.");
        }
    }

    private static void likePost(Scanner scanner) {
        System.out.println("Enter post ID to like (e.g., p1):");
        String postIdStr = scanner.nextLine();
        int postId;
        if (postIdStr.startsWith("p")) {
            try {
                postId = Integer.parseInt(postIdStr.substring(1));
            } catch (NumberFormatException e) {
                System.out.println("Invalid post ID format.");
                return;
            }
        } else {
            System.out.println("Invalid post ID format.");
            return;
        }

        if (postDAO.isPostLiked(postId, currentUser.getId())) {
            System.out.println("Already liked post. Please try again!");
        } else {
            boolean success = postDAO.likePost(postId, currentUser.getId());
            if (success) {
                System.out.println("Liked post " + postIdStr);
            } else {
                System.out.println("Failed to like post.");
            }
        }
    }

    private static void seeFollowers() {
        List<String> followers = followDAO.getFollowersIds(currentUser.getId());
        System.out.println("Your followers:");
        for (String followerId : followers) {
            System.out.println(followerId);
        }
    }

    private static void seeFollowing() {
        List<String> following = followDAO.getFollowingIds(currentUser.getId());
        System.out.println("You are following:");
        for (String followingId : following) {
            System.out.println(followingId);
        }
    }

    private static void followUser(Scanner scanner) {
        System.out.println("Input user ID to follow");
        String userIdToFollow = scanner.nextLine();

        if (userIdToFollow.equals(currentUser.getId())) {
            System.out.println("Can't follow yourself");
            return;
        }

        if (userDAO.getUserById(userIdToFollow) == null) {
            System.out.println("User does not exist.");
            return;
        }

        if (followDAO.isFollowing(userIdToFollow, currentUser.getId())) {
            System.out.println("Already followed the user. Unfollowing...");
            boolean success = followDAO.unfollowUser(userIdToFollow, currentUser.getId());
            if (success) {
                System.out.println("Unfollowed " + userIdToFollow);
            } else {
                System.out.println("Failed to unfollow user.");
            }
        } else {
            boolean success = followDAO.followUser(userIdToFollow, currentUser.getId());
            if (success) {
                System.out.println("Followed " + userIdToFollow);
            } else {
                System.out.println("Failed to follow user.");
            }
        }
    }
}