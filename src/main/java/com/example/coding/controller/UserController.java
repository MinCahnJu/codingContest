package com.example.coding.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.cdimascio.dotenv.Dotenv;

@Controller
public class UserController {
    // JDBC URL, 사용자 이름, 비밀번호
    static Dotenv dotenv = Dotenv.load();

    private static final String JDBC_URL = dotenv.get("local_JDBC_URL");
    private static final String DB_USERNAME = dotenv.get("local_DB_USERNAME");
    private static final String DB_PASSWORD = dotenv.get("local_DB_PASSWORD");

    public static void allcontestInfo(Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();
                // SQL 쿼리 실행
                String sql = "SELECT * FROM contests";
                ResultSet resultSet = statement.executeQuery(sql);

                List<String> contests = new ArrayList<String>();

                // 결과 처리
                while (resultSet.next()) {
                    contests.add(resultSet.getString("contest_name"));
                }

                session.setAttribute("contests", contests);
                model.addAttribute("contests", contests);

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return;
    }

    public static void contestInfo(String contestname, Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                // SQL 쿼리 실행
                String sql2 = "SELECT * FROM contests where contest_name = ?";
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1, contestname);
                ResultSet resultSet2 = preparedStatement.executeQuery();
                if (resultSet2.next()) {
                    session.setAttribute("contestuserid", resultSet2.getString("user_id"));
                    session.setAttribute("contestname", resultSet2.getString("contest_name"));
                    session.setAttribute("contestdescription", resultSet2.getString("description"));
                    session.setAttribute("contestpw", resultSet2.getString("contest_pw"));
                    session.setAttribute("contestid", resultSet2.getString("contest_id"));
                    System.out.println("!!++++++++++++++++++++");
                }

                String contestid = (String) session.getAttribute("contestid");

                // SQL 쿼리 실행
                String sql = "SELECT * FROM problems where contest_id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, contestid);
                ResultSet resultSet = preparedStatement.executeQuery();

                List<String> problems = new ArrayList<String>();
                while (resultSet.next()) {
                    problems.add(resultSet.getString("problem_name"));
                }

                session.setAttribute("problems", problems);
                model.addAttribute("problems", problems);

                // 리소스 해제
                resultSet2.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return;
    }

    public static void problemInfo(String problem, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql = "SELECT * FROM problems WHERE problem_name = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, problem);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String problemname = resultSet.getString("problem_name");
                    String contestid = resultSet.getString("contest_id");
                    String description = resultSet.getString("description");
                    String inputdescription = resultSet.getString("input_description");
                    String outputdescription = resultSet.getString("output_description");
                    String exampleinput = resultSet.getString("example_input");
                    String exampleoutput = resultSet.getString("example_output");
                    session.setAttribute("problemname", problemname);
                    session.setAttribute("contestid", contestid);
                    session.setAttribute("description", description);
                    session.setAttribute("inputdescription", inputdescription);
                    session.setAttribute("outputdescription", outputdescription);
                    session.setAttribute("exampleinput", exampleinput);
                    session.setAttribute("exampleoutput", exampleoutput);
                }

                String sql1 = "SELECT * FROM examples WHERE problem_name = ?";
                preparedStatement = connection.prepareStatement(sql1);
                preparedStatement.setString(1, problem);
                ResultSet resultSet2 = preparedStatement.executeQuery();

                List<String> inputexamples = new ArrayList<String>();
                List<String> outputexamples = new ArrayList<String>();

                while (resultSet2.next()) {
                    inputexamples.add(resultSet2.getString("example_input"));
                    outputexamples.add(resultSet2.getString("example_output"));
                }

                session.setAttribute("inputexamples", inputexamples);
                session.setAttribute("outputexamples", outputexamples);

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ;
    }

    public static void userInfo(Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();
                // SQL 쿼리 실행
                String sql = "SELECT * FROM users";
                ResultSet resultSet = statement.executeQuery(sql);

                List<List<String>> users = new ArrayList<>();

                // 결과 처리
                while (resultSet.next()) {
                    List<String> user = new ArrayList<>();
                    if (resultSet.getString("user_name").equals("MiC")){
                        continue;
                    }
                    user.add(resultSet.getString("user_name"));
                    user.add(resultSet.getString("user_id"));
                    user.add(resultSet.getString("user_pw"));
                    user.add(resultSet.getString("user_tell"));
                    user.add(resultSet.getString("authority"));
                    users.add(user);
                }

                session.setAttribute("users", users);
                model.addAttribute("users", users);

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return;
    }

    public static boolean containsSpecialCharacters(String input) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\s]");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static boolean containsAlphabet(String input) {
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static boolean containsNumber(String input) {
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }





    @PostMapping("/register")
    public String register(@RequestParam("username") String name, @RequestParam("ID") String id, @RequestParam("password") String pw, @RequestParam("password2") String pw2, @RequestParam("phoneNumber") String tell, Model model) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        if (!(pw.equals(pw2))){
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "register";
        }

        boolean hasSpecialCharacters = containsSpecialCharacters(pw);
        if (hasSpecialCharacters) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 특수문자 하나 이상 포함해야합니다.");
            return "register";
        }
        boolean hasAlphabet = containsAlphabet(pw);
        if (hasAlphabet) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 알파벳 하나 이상 포함해야합니다.");
            return "register";
        }
        boolean hasNumber = containsNumber(pw);
        if (hasNumber) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 숫자 하나 이상 포함해야합니다.");
            return "register";
        }

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 데이터 추가
                String sql1 = "INSERT INTO users (user_name, user_id, user_pw, user_tell, authority) VALUES (?, ?, ?, ?, ?)";

                // PreparedStatement 객체 생성
                preparedStatement = connection.prepareStatement(sql1);

                // 값 설정
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, id);
                preparedStatement.setString(3, pw);
                preparedStatement.setString(4, tell);
                if (id.equals("mathematicsincoding")) {
                    preparedStatement.setInt(5, 5);
                } else{
                    preparedStatement.setInt(5, 0);
                }

                // SQL 문 실행
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new user was inserted successfully!");
                }

                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
            model.addAttribute("error", "중복된 아이디!");
            return "register";
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("ID") String id, @RequestParam("password") String pw, HttpSession session, Model model) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null) {
                String sql = "SELECT * FROM users WHERE user_id = ? AND user_pw = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, pw);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String username = resultSet.getString("user_name");
                    String userid = resultSet.getString("user_id");
                    String usertell = resultSet.getString("user_tell");
                    String authority = resultSet.getString("authority");
                    session.setAttribute("username", username);
                    session.setAttribute("userid", userid);
                    session.setAttribute("usertell", usertell);
                    session.setAttribute("authority", authority);
                    return "redirect:/home";
                } else {
                    model.addAttribute("error", "잘못된 사용자명 또는 비밀번호입니다.");
                    return "login";
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "요청을 처리하는 동안 오류가 발생했습니다.");
            return "login";
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("error", "잘못된 사용자명 또는 비밀번호입니다.");
        return "login";
    }

    @PostMapping("/changepassword")
    public String changePassword(@RequestParam("ID") String id, @RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword, @RequestParam("confirmNewPassword") String confirmNewPassword, Model model, HttpSession session) {
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "새로운 비밀번호가 일치하지 않습니다.");
            return "change_password";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        boolean hasSpecialCharacters = containsSpecialCharacters(newPassword);
        if (hasSpecialCharacters) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 특수문자 하나 이상 포함해야합니다.");
            return "change_password";
        }
        boolean hasAlphabet = containsAlphabet(newPassword);
        if (hasAlphabet) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 알파벳 하나 이상 포함해야합니다.");
            return "change_password";
        }
        boolean hasNumber = containsNumber(newPassword);
        if (hasNumber) {
            System.out.println("The string contains special characters.");
        } else {
            model.addAttribute("error", "비밀번호에 숫자 하나 이상 포함해야합니다.");
            return "change_password";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            // 현재 비밀번호 확인
            String checkPasswordQuery = "SELECT user_pw FROM users WHERE user_id = ?";
            preparedStatement = connection.prepareStatement(checkPasswordQuery);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("user_pw");
                if (!storedPassword.equals(currentPassword)) {
                    model.addAttribute("error", "잘못된 사용자명 또는 비밀번호입니다.");
                    return "change_password";
                }
            } else {
                model.addAttribute("error", "잘못된 사용자명 또는 비밀번호입니다.");
                return "change_password";
            }

            // 비밀번호 업데이트
            String updatePasswordQuery = "UPDATE users SET user_pw = ? WHERE user_id = ?";
            preparedStatement = connection.prepareStatement(updatePasswordQuery);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, id);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                session.invalidate();
                return "login";
            } else {
                model.addAttribute("error", "변경에 실패했습니다.");
                return "change_password";
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "서버 오류");
            return "change_password";
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/edituser")
    public String edituser(@RequestParam("selectAuthority") String selectAuthority, @RequestParam("username") String username, Model model) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            // 비밀번호 업데이트
            String sql = "UPDATE users SET authority = ? WHERE user_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, selectAuthority);
            preparedStatement.setString(2, username);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated <= 0) {
                model.addAttribute("error", "변경에 실패했습니다.");
            }

            System.out.println("변경 완료");

            connection.close();
            return "redirect:/moveusers";
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "서버 오류");
            return "index";
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/deleteuser")
    public String deleteuser(@RequestParam("selectAuthority") String selectAuthority, @RequestParam("username") String username, Model model) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            System.out.println("!1");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            // 비밀번호 업데이트
            String sql = "DELETE FROM users WHERE user_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated <= 0) {
                model.addAttribute("error", "변경에 실패했습니다.");
                return "redirect:/home";
            }

            System.out.println("삭제 완료");

            connection.close();
            return "redirect:/moveusers";
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "서버 오류");
            return "index";
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/makecontest")
    public String makecontest(@RequestParam("ID") String id, @RequestParam("title") String title, @RequestParam("password") String pw, @RequestParam("password2") String pw2, @RequestParam("description") String description, Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String userid = (String) session.getAttribute("userid");

        if (!(id.equals(userid))){
            model.addAttribute("error", "본인의 아이디가 아닙니다.");
            return "make_contest";
        }

        if (!(pw.equals(pw2))){
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "make_contest";
        }

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 데이터 추가
                String sql1 = "INSERT INTO contests (user_id, contest_name, description, contest_pw) VALUES (?, ?, ?, ?)";

                // PreparedStatement 객체 생성
                preparedStatement = connection.prepareStatement(sql1);

                // 값 설정
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, pw);

                // SQL 문 실행
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new contest was inserted successfully!");
                }

                String sql = "SELECT * FROM contests WHERE user_id = ? AND contest_name = ?";

                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, title);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String contestid = resultSet.getString("contest_id");
                    String contestname = resultSet.getString("contest_name");
                    session.setAttribute("contestid", contestid);
                    session.setAttribute("contestname", contestname);
                    return "redirect:/makeproblem";
                }

                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
            model.addAttribute("error", "이미 지정된 대회명입니다.");
            return "make_contest";
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("error", "이미 지정된 대회명입니다.");
        return "make_contest";
    }

    @PostMapping("/makeproblem")
    public String makeproblem(@RequestParam("title") String title, @RequestParam("description") String description, @RequestParam("inputdescription") String inputdescription, @RequestParam("outputdescription") String outputdescription, @RequestParam("exampleinput[]") List<String> input_examples, @RequestParam("exampleoutput[]") List<String> output_examples, @RequestParam("operation") String operation, Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        session.removeAttribute("error");

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 데이터 추가
                String sql = "INSERT INTO problems (contest_id, problem_name, description, input_description, output_description, example_input,  example_output) VALUES (?, ?, ?, ?, ?, ?, ?)";

                // PreparedStatement 객체 생성
                preparedStatement = connection.prepareStatement(sql);

                String contestid = (String) session.getAttribute("contestid");

                // 값 설정
                preparedStatement.setString(1, contestid);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, inputdescription);
                preparedStatement.setString(5, outputdescription);
                preparedStatement.setString(6, input_examples.get(0));
                preparedStatement.setString(7, output_examples.get(0));

                // SQL 문 실행
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new problem was inserted successfully!");
                    System.out.println(input_examples.size());
                    System.out.println(input_examples);
                }

                
                String sql1 = "INSERT INTO examples (problem_name, example_input,  example_output) VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql1);

                for (int i = 1; i < input_examples.size(); i++) {
                    System.out.println(input_examples.get(i));
                    System.out.println(output_examples.get(i));
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, input_examples.get(i));
                    preparedStatement.setString(3, output_examples.get(i));
                    preparedStatement.addBatch();
                }

                int[] exampleRowsInserted = preparedStatement.executeBatch();
                System.out.println(exampleRowsInserted.length + " examples were inserted successfully!");

                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
            session.setAttribute("error", "이미 지정된 문제 이름입니다.");
            return "redirect:/makeproblem";
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (operation.equals("More Problem")) {
            return "redirect:/makeproblem";
        } else if (operation.equals("Contest Completion")) {
            String contestname = (String) session.getAttribute("contestname");
            contestInfo(contestname, model, session);
            return "redirect:/opencontest";
        }

        session.setAttribute("error", "이미 지정된 문제 이름입니다.");
        return "redirect:/makeproblem";
    }

    @PostMapping("/opencontest")
    public String opencontest(@RequestParam("contest") String contest, @RequestParam("password") String pw, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql4 = "SELECT * FROM contests WHERE contest_name = ?";
                preparedStatement = connection.prepareStatement(sql4);
                preparedStatement.setString(1, contest);
                ResultSet resultSet4 = preparedStatement.executeQuery();

                if (resultSet4.next()) {
                    String contestid = resultSet4.getString("contest_id");
                    String contestuserid = resultSet4.getString("user_id");
                    String contestpw = resultSet4.getString("contest_pw");
                    String contestdescription = resultSet4.getString("description");
                    session.setAttribute("contestid", contestid);
                    session.setAttribute("contestuserid", contestuserid);
                    session.setAttribute("contestpw", contestpw);
                    session.setAttribute("contestdescription", contestdescription);
                    session.setAttribute("contestname", contest);
                    System.out.println("!!!!!!!!!!!!!!!!!!!!");
                    if (!(pw.equals(contestpw))) {
                        session.setAttribute("error", "대회 비밀번호가 틀립니다.");
                        return "redirect:/moveopencontest";
                    }
                }

                String sql = "SELECT * FROM problems WHERE contest_id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, (String) session.getAttribute("contestid"));
                ResultSet resultSet = preparedStatement.executeQuery();

                List<String> list2 = new ArrayList<String>();

                // 결과 처리
                while (resultSet.next()) {
                    list2.add(resultSet.getString("problem_name"));
                }

                session.setAttribute("problems", list2);

                String sql2 = "SELECT * FROM contests WHERE contest_name = ?";
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1, contest);
                ResultSet resultSet2 = preparedStatement.executeQuery();

                if (resultSet2.next()) {
                    String contestuserid = resultSet2.getString("user_id");
                    String contestid = resultSet2.getString("contest_id");

                    session.setAttribute("contestid", contestid);

                    String sql3 = "SELECT * FROM users WHERE user_id = ?";
                    preparedStatement = connection.prepareStatement(sql3);
                    preparedStatement.setString(1, contestuserid);
                    ResultSet resultSet3 = preparedStatement.executeQuery();
    
                    if (resultSet3.next()) {
                        String contestuser = resultSet3.getString("user_name");
                        session.setAttribute("contestuser", contestuser);
                    }
                }

                session.setAttribute("contest", contest);

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");

                return "redirect:/opencontest";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return "redirect:/home";
    }

    @PostMapping("/openproblem")
    public String openproblem(@RequestParam("problem") String problem, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        
        session.removeAttribute("error");
        session.removeAttribute("message");
        session.removeAttribute("result");

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql = "SELECT * FROM problems WHERE problem_name = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, problem);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String problemname = resultSet.getString("problem_name");
                    String contestid = resultSet.getString("contest_id");
                    String description = resultSet.getString("description");
                    String inputdescription = resultSet.getString("input_description");
                    String outputdescription = resultSet.getString("output_description");
                    String exampleinput = resultSet.getString("example_input");
                    String exampleoutput = resultSet.getString("example_output");
                    session.setAttribute("problemname", problemname);
                    session.setAttribute("contestid", contestid);
                    session.setAttribute("description", description);
                    session.setAttribute("inputdescription", inputdescription);
                    session.setAttribute("outputdescription", outputdescription);
                    session.setAttribute("exampleinput", exampleinput);
                    session.setAttribute("exampleoutput", exampleoutput);
                    return "redirect:/openproblem";
                }

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");
                model.addAttribute("error", "잘못된 문제명입니다.");
                return "open_contest";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return "redirect:/home";
    }

    @PostMapping("/deletecontest")
    public String deletecontest(@RequestParam("contest") String contest, @RequestParam("contestid") String contestid, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        
        session.removeAttribute("error");
        session.removeAttribute("message");
        session.removeAttribute("result");

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql = "DELETE FROM contests WHERE contest_name = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, contest);
                int rowsAffected = preparedStatement.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("대회 삭제 완");
                }

                String sql2 = "DELETE FROM problems WHERE contest_id = ?";
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1, contestid);
                int rowsAffected2 = preparedStatement.executeUpdate();
                
                if (rowsAffected2 > 0) {
                    System.out.println("문제 삭제 완");
                }

                statement.close();
                connection.close();
                System.out.println("종료 !");
                model.addAttribute("error", "잘못된 문제명입니다.");
                return "redirect:/home";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return "redirect:/home";
    }

    @PostMapping("/deleteproblem")
    public String deleteproblem(@RequestParam("problem") String problem, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        
        session.removeAttribute("error");
        session.removeAttribute("message");
        session.removeAttribute("result");

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql = "DELETE FROM problems WHERE problem_name = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, problem);
                int rowsAffected = preparedStatement.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("문제 삭제 완");
                }

                String contestname = (String) session.getAttribute("contestname");
                contestInfo(contestname, model, session);


                statement.close();
                connection.close();
                System.out.println("종료 !");
                model.addAttribute("error", "잘못된 문제명입니다.");
                return "redirect:/opencontest";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return "redirect:/home";
    }

    @PostMapping("/editproblem")
    public String editproblem(@RequestParam("title") String title, @RequestParam("description") String description, @RequestParam("inputdescription") String inputdescription, @RequestParam("outputdescription") String outputdescription, @RequestParam("exampleinput[]") List<String> input_examples, @RequestParam("exampleoutput[]") List<String> output_examples, Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");

                String problemname = (String) session.getAttribute("problemname");
                String contestid = (String) session.getAttribute("contestid");
                String contestname = (String) session.getAttribute("contestname");

                // 데이터 추가
                String sql = "UPDATE problems SET contest_id = ?, problem_name = ?, description = ?, input_description = ?, output_description = ?, example_input = ?, example_output = ? WHERE problem_name = ?";

                // PreparedStatement 객체 생성
                preparedStatement = connection.prepareStatement(sql);

                // 값 설정
                preparedStatement.setString(1, contestid);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, inputdescription);
                preparedStatement.setString(5, outputdescription);
                preparedStatement.setString(6, input_examples.get(0));
                preparedStatement.setString(7, output_examples.get(0));
                preparedStatement.setString(8, problemname);

                // SQL 문 실행
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("문제 변경 완!");
                }

                String sql2 = "DELETE FROM examples WHERE problem_name = ?";
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1, title);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("문제 삭제 완");
                }

                String sql1 = "INSERT INTO examples (problem_name, example_input,  example_output) VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql1);

                for (int i = 1; i < input_examples.size(); i++) {
                    System.out.println(input_examples.get(i)+","+output_examples.get(i));
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, input_examples.get(i));
                    preparedStatement.setString(3, output_examples.get(i));
                    preparedStatement.addBatch();
                }

                int[] exampleRowsInserted = preparedStatement.executeBatch();
                System.out.println(exampleRowsInserted.length + " examples were inserted successfully!");

                contestInfo(contestname, model, session);
                problemInfo(title, model, session);
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                statement.close();
                connection.close();
                System.out.println("종료 !");
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
            model.addAttribute("error", "이미 지정된 문제 이름입니다.");
            return "redirect:/editproblem";
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/openproblem";
    }

    @PostMapping("/editcontest")
    public String editcontest(@RequestParam("title") String title, @RequestParam("password") String pw, @RequestParam("password2") String pw2, @RequestParam("description") String description, Model model, HttpSession session) {
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        if (!(pw.equals(pw2))){
            session.setAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "redirect:/editcontest";
        }

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 데이터 추가
                String sql1 = "UPDATE contests SET contest_name = ?, contest_pw = ?, description=? where contest_id =?";

                String contestid = (String) session.getAttribute("contestid");

                // PreparedStatement 객체 생성
                preparedStatement = connection.prepareStatement(sql1);

                // 값 설정
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, pw);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, contestid);

                // SQL 문 실행
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A contest was inserted successfully!");
                }

                session.setAttribute("contestname", title);
                session.setAttribute("contestdescription", description);
                session.setAttribute("contestpw", pw);

                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                statement.close();
                connection.close();
                System.out.println("종료 !");
                return "redirect:/opencontest";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
            model.addAttribute("error", "이미 지정된 대회명입니다.");
            return "redirect:/editcontest";
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("error", "이미 지정된 대회명입니다.");
        return "redirect:/editcontest";
    }

    @PostMapping("/submitCode")
    public String submitCode(@RequestParam("language") String language, @RequestParam("code") String code, Model model, HttpSession session) {
        session.removeAttribute("result");

        String input = (String) session.getAttribute("exampleinput");
        @SuppressWarnings("unchecked")
        List<String> inputexamples = (List<String>) session.getAttribute("inputexamples");
        @SuppressWarnings("unchecked")
        List<String> outputexamples = (List<String>) session.getAttribute("outputexamples");

        int total = inputexamples.size() + 1;
        String result = evaluateCode(total, language, code, input, (String) session.getAttribute("exampleoutput"), inputexamples, outputexamples, session);

        session.setAttribute("result", result);

        return "redirect:/openproblem";
    }

    private String evaluateCode(int total, String language, String code, String exampleInput, String exampleOutput, List<String> inputExamples, List<String> outputExamples, HttpSession session) {
        int count = 0;

        if (compileAndRunCode(language, code, exampleInput, exampleOutput, session).equals("Correct")) {
            count++;
        } else if (!(compileAndRunCode(language, code, exampleInput, exampleOutput, session).equals("Incorrect"))) {
            return compileAndRunCode(language, code, exampleInput, exampleOutput, session);
        }

        for (int i = 0; i < inputExamples.size(); i++) {
            if (compileAndRunCode(language, code, inputExamples.get(i), outputExamples.get(i), session).equals("Correct")) {
                count++;
            } else if (!(compileAndRunCode(language, code, inputExamples.get(i), outputExamples.get(i), session).equals("Incorrect"))) {
                return compileAndRunCode(language, code, inputExamples.get(i), outputExamples.get(i), session);
            }
        }

        int score = (int) (((double) count / total) * 100);
        return String.valueOf(score)+"점";
    }

    private String compileAndRunCode(String language, String code, String input, String exampleOutput, HttpSession session) {
        switch (language.toLowerCase()) {
            case "java":
                return compileAndRunJavaCode(code, input, exampleOutput);
            case "python":
                return runPythonCode(code, input, exampleOutput);
            case "c":
                return compileAndRunCCode(code, input, exampleOutput);
            default:
                return "Unsupported Language";
        }
    }

    private String compileAndRunJavaCode(String code, String input, String exampleOutput) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);

            JavaFileObject javaFileObject = new InMemoryJavaFileObject("Main", code);
            Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(javaFileObject);

            CustomJavaFileManager fileManager = new CustomJavaFileManager(standardFileManager);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

            // 컴파일 시간 제한 설정 (5초)
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(task::call);
            boolean success;
            try {
                success = future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                return "Compilation Timeout";
            } finally {
                executor.shutdown();
            }

            if (!success) {
                return formatCompilationError(diagnostics);
            }

            return runCompiledJavaCode(input, exampleOutput, fileManager);

        } catch (Exception e) {
            e.printStackTrace();
            return "Execution Error: " + e.getMessage();
        }
    }

    private String runCompiledJavaCode(String input, String expectedOutput, CustomJavaFileManager fileManager) throws Exception {
        DynamicClassLoader classLoader = new DynamicClassLoader();
        fileManager.getCompiledClassObjects().forEach(classLoader::addClassData);

        Class<?> clazz = classLoader.findClass("Main");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PrintStream printStream = new PrintStream(outputStream);
                InputStream inputStream = new ByteArrayInputStream(input.getBytes())) {

            System.setOut(printStream);
            System.setIn(inputStream);

            // 실행 시간 제한 설정 (5초)
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> future = executor.submit(() -> {
                try {
                    clazz.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                return "Execution Timeout";
            } finally {
                executor.shutdownNow();
            }

            String output = outputStream.toString().trim();
            return output.equals(expectedOutput.trim()) ? "Correct" : "Incorrect";

        } finally {
            System.setOut(System.out);
            System.setIn(System.in);
        }
    }

    private String runPythonCode(String code, String input, String expectedOutput) {
        AtomicReference<Process> processRef = new AtomicReference<>();
        try {
            // 파이썬 코드 실행
            ProcessBuilder builder = new ProcessBuilder("python", "-c", code);
            Process process = builder.start();
            processRef.set(process);

            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();

            stdin.write(input.getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String output = reader.lines().collect(Collectors.joining("\n")).trim();
            reader.close();

            // 실행 시간 제한 설정 (5초)
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> process.waitFor());

            try {
                if (future.get(1, TimeUnit.SECONDS) != 0) {
                    return "Execution Error: Non-zero exit code";
                }
            } catch (TimeoutException e) {
                process.destroyForcibly();
                return "Execution Timeout";
            } finally {
                executor.shutdownNow();
            }

            return output.equals(expectedOutput.trim()) ? "Correct" : "Incorrect";

        } catch (Exception e) {
            if (processRef.get() != null) processRef.get().destroyForcibly();
            e.printStackTrace();
            return "Execution Error: " + e.getMessage();
        }
    }

    private String compileAndRunCCode(String code, String input, String expectedOutput) {
        AtomicReference<Process> compileProcessRef = new AtomicReference<>();
        AtomicReference<Process> runProcessRef = new AtomicReference<>();
        try {
            // C 소스 파일 저장
            Path sourceFile = Files.createTempFile("program", ".c");
            Files.write(sourceFile, code.getBytes());

            // C 컴파일
            ProcessBuilder compileBuilder = new ProcessBuilder("gcc", sourceFile.toString(), "-o", sourceFile.toString() + ".out");
            Process compileProcess = compileBuilder.start();
            compileProcessRef.set(compileProcess);
            compileProcess.waitFor();

            if (compileProcess.exitValue() != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                String errorOutput = errorReader.lines().collect(Collectors.joining("\n"));
                errorReader.close();
                return "Compilation Error: " + errorOutput;
            }

            // C 실행
            ProcessBuilder runBuilder = new ProcessBuilder(sourceFile.toString() + ".out");
            Process runProcess = runBuilder.start();
            runProcessRef.set(runProcess);

            OutputStream stdin = runProcess.getOutputStream();
            InputStream stdout = runProcess.getInputStream();

            stdin.write(input.getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String output = reader.lines().collect(Collectors.joining("\n")).trim();
            reader.close();

            // 실행 시간 제한 설정 (5초)
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());

            try {
                if (future.get(5, TimeUnit.SECONDS) != 0) {
                    return "Execution Error: Non-zero exit code";
                }
            } catch (TimeoutException e) {
                runProcess.destroyForcibly();
                return "Execution Timeout";
            } finally {
                executor.shutdownNow();
            }

            return output.equals(expectedOutput.trim()) ? "Correct" : "Incorrect";

        } catch (Exception e) {
            if (compileProcessRef.get() != null) compileProcessRef.get().destroyForcibly();
            if (runProcessRef.get() != null) runProcessRef.get().destroyForcibly();
            e.printStackTrace();
            return "Execution Error: " + e.getMessage();
        }
    }

    private String formatCompilationError(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder errorMsg = new StringBuilder("Compilation Error:\n");
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            errorMsg.append(diagnostic.getMessage(null)).append("\n");
        }
        return errorMsg.toString();
    }

    // 메모리 내에서 소스 파일을 표현하는 클래스
    class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String sourceCode;

        public InMemoryJavaFileObject(String className, String sourceCode) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = sourceCode;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return sourceCode;
        }
    }

    // 메모리 내에서 컴파일된 클래스를 저장하는 클래스
    class ByteArrayJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream;

        protected ByteArrayJavaFileObject(String name, Kind kind) {
            super(URI.create("bytes:///" + name), kind);
            outputStream = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }

        public byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    // 커스텀 JavaFileManager
    class CustomJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, ByteArrayJavaFileObject> compiledClassObjects = new HashMap<>();

        protected CustomJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            ByteArrayJavaFileObject javaFileObject = new ByteArrayJavaFileObject(className, kind);
            compiledClassObjects.put(className, javaFileObject);
            return javaFileObject;
        }

        public Map<String, ByteArrayJavaFileObject> getCompiledClassObjects() {
            return compiledClassObjects;
        }
    }

    // 메모리 내에서 클래스를 로드하는 클래스 로더
    class DynamicClassLoader extends ClassLoader {
        private final Map<String, byte[]> classData = new HashMap<>();

        public void addClassData(String className, ByteArrayJavaFileObject javaFileObject) {
            classData.put(className, javaFileObject.getBytes());
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] byteCode = classData.get(name);
            if (byteCode == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, byteCode, 0, byteCode.length);
        }
    }






    @PostMapping("/home")
    public String home2(Model model, HttpSession session) {
        allcontestInfo(model, session);
        String username = (String) session.getAttribute("username");
        String authority = (String) session.getAttribute("authority");
        model.addAttribute("username", username);
        model.addAttribute("authority", authority);
        return "index";
    }

    @PostMapping("/movemakeproblem")
    public String movemakeproblem(Model model, HttpSession session) {
        String contestid = (String) session.getAttribute("contestid");
        String contestname = (String) session.getAttribute("contestname");
        model.addAttribute("contestid", contestid);
        model.addAttribute("contestname", contestname);
        return "make_problem";
    }

    @PostMapping("/moveregister")
    public String moveregister() {
        return "register";
    }

    @PostMapping("/movemakecontest")
    public String movemakecon(Model model, HttpSession session) {
        return "make_contest";
    }

    @PostMapping("/moveeditproblem")
    public String moveeditproblem(@RequestParam("problem") String problem, Model model, HttpSession session) {
        problemInfo(problem, model, session);
        @SuppressWarnings("unchecked")
        List<String> inputexamples = (List<String>) session.getAttribute("inputexamples");
        @SuppressWarnings("unchecked")
        List<String> outputexamples = (List<String>) session.getAttribute("outputexamples");
        String problemname = (String) session.getAttribute("problemname");
        String contestid = (String) session.getAttribute("contestid");
        String description = (String) session.getAttribute("description");
        String inputdescription = (String) session.getAttribute("inputdescription");
        String outputdescription = (String) session.getAttribute("outputdescription");
        String exampleinput = (String) session.getAttribute("exampleinput");
        String exampleoutput = (String) session.getAttribute("exampleoutput");
        String username = (String) session.getAttribute("username");
        String contestname = (String) session.getAttribute("contestname");
        model.addAttribute("problemname", problemname);
        model.addAttribute("contestid", contestid);
        model.addAttribute("description", description);
        model.addAttribute("inputdescription", inputdescription);
        model.addAttribute("outputdescription", outputdescription);
        model.addAttribute("exampleinput", exampleinput);
        model.addAttribute("exampleoutput", exampleoutput);
        model.addAttribute("inputexamples", inputexamples);
        model.addAttribute("outputexamples", outputexamples);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contestname);
        return "edit_problem";
    }

    @PostMapping("/moveeditcontest")
    public String moveeditcontest(Model model, HttpSession session) {
        String contestid = (String) session.getAttribute("contestid");
        String contestdescription = (String) session.getAttribute("contestdescription");
        String contestpw = (String) session.getAttribute("contestpw");
        String contestuserid = (String) session.getAttribute("contestuserid");
        String username = (String) session.getAttribute("username");
        String contestname = (String) session.getAttribute("contestname");
        model.addAttribute("contestid", contestid);
        model.addAttribute("contestdescription", contestdescription);
        model.addAttribute("contestpw", contestpw);
        model.addAttribute("contestuserid", contestuserid);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contestname);
        return "edit_contest";
    }

    @PostMapping("/movelogin")
    public String movelogin() {
        return "login";
    }

    @PostMapping("/movechangepassword")
    public String movechangepassword() {
        return "change_password";
    }

    @PostMapping("/moveusers")
    public String moveusers(Model model, HttpSession session) {
        userInfo(model, session);
        String username = (String) session.getAttribute("username");
        String authority = (String) session.getAttribute("authority");
        model.addAttribute("username", username);
        model.addAttribute("authority", authority);
        return "users";
    }

    @PostMapping("/moveopencontest")
    public String moveopencontest(@RequestParam("contest") String contest, Model model, HttpSession session) {
        contestInfo(contest, model, session);
        String contestuserid = (String) session.getAttribute("contestuserid");
        String userid = (String) session.getAttribute("userid");
        String authority = (String) session.getAttribute("authority");
        String username = (String) session.getAttribute("username");
        String contestpw = (String) session.getAttribute("contestpw");
        session.setAttribute("contestname",contest);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contest);
        if (contestpw.equals("open")) {
            try {
                String encodedContest = URLEncoder.encode(contest, "UTF-8");
                return "redirect:/opencontest2?contest=" + encodedContest;
            } catch (UnsupportedEncodingException e) {
                // 에러 처리
                e.printStackTrace();
                return "check_pw";
            }
        }
        if (authority == null) {
            return "check_pw";
        }
        if (contestuserid.equals(userid) | authority.equals("5") | authority.equals("4")) {
            try {
                String encodedContest = URLEncoder.encode(contest, "UTF-8");
                return "redirect:/opencontest2?contest=" + encodedContest;
            } catch (UnsupportedEncodingException e) {
                // 에러 처리
                e.printStackTrace();
                return "check_pw";
            }
        }
        return "check_pw";
    }




    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        allcontestInfo(model, session);
        String username = (String) session.getAttribute("username");
        String userid = (String) session.getAttribute("userid");
        String usertell = (String) session.getAttribute("usertell");
        String admin = (String) session.getAttribute("admin");
        String authority = (String) session.getAttribute("authority");
        model.addAttribute("username", username);
        model.addAttribute("userid", userid);
        model.addAttribute("usertell", usertell);
        model.addAttribute("admin", admin);
        model.addAttribute("authority", authority);
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String userid = (String) session.getAttribute("userid");
        String usertell = (String) session.getAttribute("usertell");
        String authority = (String) session.getAttribute("authority");
        model.addAttribute("username", username);
        model.addAttribute("userid", userid);
        model.addAttribute("usertell", usertell);
        model.addAttribute("authority", authority);
        return "profile";
    }

    @GetMapping("/makeproblem")
    public String makeproblem(Model model, HttpSession session) {
        String contestid = (String) session.getAttribute("contestid");
        String contestname = (String) session.getAttribute("contest");
        String error = (String) session.getAttribute("error");
        model.addAttribute("contestid", contestid);
        model.addAttribute("contest", contestname);
        model.addAttribute("error", error);
        return "make_problem";
    }

    @GetMapping("/opencontest")
    public String opencontest(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> problems = (List<String>) session.getAttribute("problems");
        String username = (String) session.getAttribute("username");
        String authority = (String) session.getAttribute("authority");
        String contestuser = (String) session.getAttribute("contestuser");
        String contestname = (String) session.getAttribute("contestname");
        String contestid = (String) session.getAttribute("contestid");
        String contestuserid = (String) session.getAttribute("contestuserid");
        String contestpw = (String) session.getAttribute("contestpw");
        String contestdescription = (String) session.getAttribute("contestdescription");
        model.addAttribute("username", username);
        model.addAttribute("problems", problems);
        model.addAttribute("authority", authority);
        model.addAttribute("contestuser", contestuser);
        model.addAttribute("contestname", contestname);
        model.addAttribute("contestid", contestid);
        model.addAttribute("contestuserid", contestuserid);
        model.addAttribute("contestpw", contestpw);
        model.addAttribute("contestdescription", contestdescription);
        return "open_contest";
    }

    @GetMapping("/openproblem")
    public String openproblem(Model model, HttpSession session) {
        String problemname = (String) session.getAttribute("problemname");
        String contestid = (String) session.getAttribute("contestid");
        String description = (String) session.getAttribute("description");
        String inputdescription = (String) session.getAttribute("inputdescription");
        String outputdescription = (String) session.getAttribute("outputdescription");
        String exampleinput = (String) session.getAttribute("exampleinput");
        String exampleoutput = (String) session.getAttribute("exampleoutput");
        String username = (String) session.getAttribute("username");
        String contestuser = (String) session.getAttribute("contestuser");
        String authority = (String) session.getAttribute("authority");
        String error = (String) session.getAttribute("error");
        String message = (String) session.getAttribute("message");
        String result = (String) session.getAttribute("result");
        problemInfo(problemname, model, session);
        model.addAttribute("problemname", problemname);
        model.addAttribute("contestid", contestid);
        model.addAttribute("description", description);
        model.addAttribute("inputdescription", inputdescription);
        model.addAttribute("outputdescription", outputdescription);
        model.addAttribute("exampleinput", exampleinput);
        model.addAttribute("exampleoutput", exampleoutput);
        model.addAttribute("username", username);
        model.addAttribute("contestuser", contestuser);
        model.addAttribute("authority", authority);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
        model.addAttribute("result", result);
        return "open_problem";
    }

    @GetMapping("/editproblem")
    public String editproblem(@RequestParam("problem") String problem, Model model, HttpSession session) {
        problemInfo(problem, model, session);
        @SuppressWarnings("unchecked")
        List<String> inputexamples = (List<String>) session.getAttribute("inputexamples");
        @SuppressWarnings("unchecked")
        List<String> outputexamples = (List<String>) session.getAttribute("outputexamples");
        String problemname = (String) session.getAttribute("problemname");
        String contestid = (String) session.getAttribute("contestid");
        String description = (String) session.getAttribute("description");
        String inputdescription = (String) session.getAttribute("inputdescription");
        String outputdescription = (String) session.getAttribute("outputdescription");
        String exampleinput = (String) session.getAttribute("exampleinput");
        String exampleoutput = (String) session.getAttribute("exampleoutput");
        String username = (String) session.getAttribute("username");
        String contestname = (String) session.getAttribute("contestname");
        model.addAttribute("problemname", problemname);
        model.addAttribute("contestid", contestid);
        model.addAttribute("description", description);
        model.addAttribute("inputdescription", inputdescription);
        model.addAttribute("outputdescription", outputdescription);
        model.addAttribute("exampleinput", exampleinput);
        model.addAttribute("exampleoutput", exampleoutput);
        model.addAttribute("inputexamples", inputexamples);
        model.addAttribute("outputexamples", outputexamples);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contestname);
        return "edit_problem";
    }

    @GetMapping("/editcontest")
    public String editcontest(Model model, HttpSession session) {
        String contestid = (String) session.getAttribute("contestid");
        String contestdescription = (String) session.getAttribute("contestdescription");
        String contestpw = (String) session.getAttribute("contestpw");
        String contestuserid = (String) session.getAttribute("contestuserid");
        String username = (String) session.getAttribute("username");
        String contestname = (String) session.getAttribute("contestname");
        String error = (String) session.getAttribute("error");
        model.addAttribute("error", error);
        model.addAttribute("contestid", contestid);
        model.addAttribute("contestdescription", contestdescription);
        model.addAttribute("contestpw", contestpw);
        model.addAttribute("contestuserid", contestuserid);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contestname);
        return "edit_contest";
    }

    @GetMapping("/moveusers")
    public String users(Model model, HttpSession session) {
        userInfo(model, session);
        String username = (String) session.getAttribute("username");
        String authority = (String) session.getAttribute("authority");
        model.addAttribute("username", username);
        model.addAttribute("authority", authority);
        return "users";
    }

    @GetMapping("/moveopencontest")
    public String moveopencontest2(Model model, HttpSession session) {
        String contest = (String) session.getAttribute("contestname");
        String username = (String) session.getAttribute("username");
        String error = (String) session.getAttribute("error");
        model.addAttribute("error", error);
        model.addAttribute("username", username);
        model.addAttribute("contestname", contest);
        return "check_pw";
    }

    @GetMapping("/opencontest2")
    public String opencontest2(@RequestParam("contest") String contest, Model model, HttpSession session){
        // Connection, Statement, preparedStatement 객체를 선언
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 데이터베이스 연결
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);

            if (connection != null){
                System.out.println("성공");
                // 쿼리 실행을 위한 Statement 객체 생성
                statement = connection.createStatement();

                String sql4 = "SELECT * FROM contests WHERE contest_name = ?";
                preparedStatement = connection.prepareStatement(sql4);
                preparedStatement.setString(1, contest);
                ResultSet resultSet4 = preparedStatement.executeQuery();

                if (resultSet4.next()) {
                    String contestid = resultSet4.getString("contest_id");
                    String contestuserid = resultSet4.getString("user_id");
                    String contestpw = resultSet4.getString("contest_pw");
                    String contestdescription = resultSet4.getString("description");
                    session.setAttribute("contestid", contestid);
                    session.setAttribute("contestuserid", contestuserid);
                    session.setAttribute("contestpw", contestpw);
                    session.setAttribute("contestdescription", contestdescription);
                    session.setAttribute("contestname", contest);
                    System.out.println("!!!!!!!!!!!!!!!!!!!!");
                }

                String sql = "SELECT * FROM problems WHERE contest_id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, (String) session.getAttribute("contestid"));
                ResultSet resultSet = preparedStatement.executeQuery();

                List<String> list2 = new ArrayList<String>();

                // 결과 처리
                while (resultSet.next()) {
                    list2.add(resultSet.getString("problem_name"));
                }

                session.setAttribute("problems", list2);

                String sql2 = "SELECT * FROM contests WHERE contest_name = ?";
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1, contest);
                ResultSet resultSet2 = preparedStatement.executeQuery();

                if (resultSet2.next()) {
                    String contestuserid = resultSet2.getString("user_id");
                    String contestid = resultSet2.getString("contest_id");

                    session.setAttribute("contestid", contestid);

                    String sql3 = "SELECT * FROM users WHERE user_id = ?";
                    preparedStatement = connection.prepareStatement(sql3);
                    preparedStatement.setString(1, contestuserid);
                    ResultSet resultSet3 = preparedStatement.executeQuery();
    
                    if (resultSet3.next()) {
                        String contestuser = resultSet3.getString("user_name");
                        session.setAttribute("contestuser", contestuser);
                    }
                }

                session.setAttribute("contest", contest);

                // 리소스 해제
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("종료 !");

                return "redirect:/opencontest";
            } else{System.out.println("실패");}
        } catch (SQLException e) {
            System.out.println( "[SQL 오류] > " + e.getMessage() );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println( "[클래스 오류]" + e.getMessage() );
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return "redirect:/home";
    }

}