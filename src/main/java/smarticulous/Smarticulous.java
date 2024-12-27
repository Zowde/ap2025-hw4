package smarticulous;

import smarticulous.db.Exercise;
import smarticulous.db.Submission;
import smarticulous.db.User;
import smarticulous.db.Exercise.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * The Smarticulous class, implementing a grading system.
 */
public class Smarticulous {

    /**
     * The connection to the underlying DB.
     * <p>
     * null if the db has not yet been opened.
     */
    Connection db;

    /**
     * Open the {@link Smarticulous} SQLite database.
     * <p>
     * This should open the database, creating a new one if necessary, and set the {@link #db} field
     * to the new connection.
     * <p>
     * The open method should make sure the database contains the following tables, creating them if necessary:
     *
     * <table>
     *   <caption><em>Table name: <strong>User</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>UserId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Username</td><td>Text</td></tr>
     *   <tr><td>Firstname</td><td>Text</td></tr>
     *   <tr><td>Lastname</td><td>Text</td></tr>
     *   <tr><td>Password</td><td>Text</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Exercise</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>DueDate</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Question</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>Desc</td><td>Text</td></tr>
     *   <tr><td>Points</td><td>Integer</td></tr>
     * </table>
     * In this table the combination of ExerciseId and QuestionId together comprise the primary key.
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Submission</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>UserId</td><td>Integer</td></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>SubmissionTime</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>QuestionGrade</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Grade</td><td>Real</td></tr>
     * </table>
     * In this table the combination of SubmissionId and QuestionId together comprise the primary key.
     *
     * @param dburl The JDBC url of the database to open (will be of the form "jdbc:sqlite:...")
     * @return the new connection
     * @throws SQLException
     * 
     */
    public Connection openDB(String dburl) throws SQLException {
          db = DriverManager.getConnection(dburl);
         Statement st = db.createStatement();
         st.executeUpdate("CREATE TABLE IF NOT EXISTS User (UserId INTEGER PRIMARY KEY , Username TEXT UNIQUE , Firstname TEXT, Lastname TEXT, Password TEXT);");
         st.executeUpdate("CREATE TABLE IF NOT EXISTS Exercise (ExerciseId INTEGER PRIMARY KEY , Name TEXT, DueDate INTEGER);");
         st.executeUpdate("CREATE TABLE IF NOT EXISTS Question (ExerciseId INTEGER, QuestionId INTEGER, Name TEXT, Desc TEXT, Points INTEGER, PRIMARY KEY (ExerciseId, QuestionId));");
         st.executeUpdate("CREATE TABLE IF NOT EXISTS Submission (SubmissionId INTEGER PRIMARY KEY , UserId INTEGER , ExerciseId INTEGER, SubmissionTime INTEGER);");
         st.executeUpdate("CREATE TABLE IF NOT EXISTS QuestionGrade (SubmissionId INTEGER , QuestionId INTEGER , Grade REAL, PRIMARY KEY (SubmissionId, QuestionId));");
         Connection db2 = db;
        return db2;
    }


    /**
     * Close the DB if it is open.
     *
     * @throws SQLException
     */
    public void closeDB() throws SQLException {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    // =========== User Management =============

    /**
     * Add a user to the database / modify an existing user.
     * <p>
     * Add the user to the database if they don't exist. If a user with user.username does exist,
     * update their password and firstname/lastname in the database.
     *
     * @param user
     * @param password
     * @return the userid.
     * @throws SQLException
     */
    public int addOrUpdateUser(User user, String password) throws SQLException {
        //"jdbc:sqlite:file:633724?mode=memory&cache=shared"

        String exist = "SELECT UserId FROM User WHERE User.Username = ? ";
        PreparedStatement ps1 = db.prepareStatement(exist);
            ps1.setString(1, user.username);
            ResultSet rs1 = ps1.executeQuery();
                  if(rs1.next())//the username exist
                  {
                    String updatepass = "UPDATE User SET Password = ? WHERE Username=?;" ;
                    PreparedStatement ps2 = db.prepareStatement(updatepass);
                    ps2.setString(1,password);
                    ps2.setString(2, user.username);
                    ps2.executeUpdate();
                  }
                  else{/// adding the username
                    String insertuser = "INSERT INTO User (Username,Firstname,Lastname,password) VALUES(?,?,?,?);";
                    PreparedStatement ps3 = db.prepareStatement(insertuser);
                    ps3.setString(1, user.username);
                    ps3.setString(2, user.firstname);
                    ps3.setString(3, user.lastname);
                    ps3.setString(4, password);
                    ps3.executeUpdate();
                  }
                  String getid ="SELECT UserId FROM User WHERE User.username = ?;";
                  PreparedStatement ps4 = db.prepareStatement(getid);
                  ps4.setString(1, user.username);
                  ResultSet res = ps4.executeQuery();
                  res.next();
                  return res.getInt("UserId");
                  

    }
    

    /**
     * Verify a user's login credentials.
     *
     * @param username
     * @param password
     * @return true if the user exists in the database and the password matches; false otherwise.
     * @throws SQLException
     * <p>
     * Note: this is totally insecure. For real-life password checking, it's important to store only
     * a password hash
     * @see <a href="https://crackstation.net/hashing-security.htm">How to Hash Passwords Properly</a>
     */
    public boolean verifyLogin(String username, String password) throws SQLException {
        String vString = "SELECT Username , Password FROM User WHERE Username=? AND Password=?;";
        PreparedStatement ps = db.prepareStatement(vString);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if(rs.next())
        {
            return true;
        }
      return false;
    }

    // =========== Exercise Management =============

    /**
     * Add an exercise to the database.
     *
     * @param exercise
     * @return the new exercise id, or -1 if an exercise with this id already existed in the database.
     * @throws SQLException
     */
    public int addExercise(Exercise exercise) throws SQLException {
        // Check if the exercise already exists
        String exid = "SELECT ExerciseId FROM Exercise WHERE Name=? AND DueDate=?;";
        PreparedStatement ps1 = db.prepareStatement(exid);
        ps1.setString(1, exercise.name);
        ps1.setString(2, String.valueOf(exercise.dueDate.getTime()));
        ResultSet rs = ps1.executeQuery();
        if (!rs.next()) {  // Exercise doesn't exist
            // Insert the new exercise
            String addexer = "INSERT INTO Exercise (Name, DueDate) VALUES(?,?);";
            PreparedStatement ps2 = db.prepareStatement(addexer);
            ps2.setString(1, exercise.name);
            ps2.setString(2, String.valueOf(exercise.dueDate.getTime()));
            ps2.executeUpdate();
            ps1.setString(1, exercise.name);
            ps1.setString(2, String.valueOf(exercise.dueDate.getTime()));
            ResultSet rs2 = ps1.executeQuery();
            rs2.next();
           int id =  rs2.getInt("ExerciseId");// the exrciseid
           addQuestion(exercise.questions,id);

                return id;
            

           
        }
    
        return -1;  // Exercise already exists
    }
    
    public void addQuestion(List<Question> ls,int id) throws SQLException
    {
       for(Question iter :ls) {

              String insertquestionsql = "INSERT INTO Question (ExerciseId,Name ,  Desc , Points) VALUES(?,?,?,?);";
              PreparedStatement ps = db.prepareStatement(insertquestionsql);
              ps.setInt(1,id );
              ps.setString(2, iter.name);
              ps.setString(3, iter.desc);
              ps.setInt(4, iter.points);
              ps.executeUpdate();

        }
     
    }

    /**
     * Return a list of all the exercises in the database.
     * <p>
     * The list should be sorted by exercise id.
     *
     * @return list of all exercises.
     * @throws SQLException
     */
    public List<Exercise> loadExercises() throws SQLException {
        // TODO: Implement
        String exercisetable = "SELECT * FROM Exercise ORDER BY ExerciseId;";
        PreparedStatement ps = db.prepareStatement(exercisetable);
        ResultSet rs = ps.executeQuery();
        List<Exercise> ls = new ArrayList<>();
        while (rs.next()) {

          int id = rs.getInt("ExerciseId");
        String name =   rs.getString("Name");
        java.util.Date d = new Date(rs.getLong("DueDate"));
        Exercise ex = new Exercise(id, name, d);
        addQuestion(ex.questions, id);
        List<Question> ls2 =getquestionExercise(ex);
        ex.questions = ls2;
         ls.add(ex);
            
        }
        rs.close();
        return ls;
    }

    public List<Question> getquestionExercise(Exercise ex) throws SQLException
    {
        String exercisetable = "SELECT Name,Desc,Points FROM Question Where ExerciseId=?;";
        PreparedStatement ps = db.prepareStatement(exercisetable);
        ps.setInt(1, ex.id);
        ResultSet rs = ps.executeQuery();
        List<Question> ls = new ArrayList<>();
        while (rs.next()) {
        String name = rs.getString("Name");
        String desc = rs.getString("Desc");
        int points = rs.getInt("Points");
        Question q = ex.new Question(name, desc, points);
        ls.add(q);
        
        }
        return ls;

    }

    // ========== Submission Storage ===============

    /**
     * Store a submission in the database.
     * The id field of the submission will be ignored if it is -1.
     * <p>
     * Return -1 if the corresponding user doesn't exist in the database.
     *
     * @param submission
     * @return the submission id.
     * @throws SQLException
     */
    public int storeSubmission(Submission submission) throws SQLException {
        // TODO: Implement
        /*check if the user exist */
         String userexist = "SELECT UserId FROM User Where Username = ?;";
         PreparedStatement ps1 = db.prepareStatement(userexist);
         ps1.setString(1,submission.user.username);
         ResultSet rs1 = ps1.executeQuery();

       /* checl if the exercise exist */
       String exerciseexist = "SELECT ExerciseId FROM Exercise Where ExerciseId = ?;";
       PreparedStatement ps2 = db.prepareStatement(exerciseexist);
       ps2.setInt(1,submission.exercise.id);
       ResultSet rs2 = ps2.executeQuery();
         if(rs1.next() && rs2.next()){ // the user and exercise are valid
            if (submission.id == -1) {
                // Insert new submission
                String insertSubmissionQuery = "INSERT INTO Submission (UserId, ExerciseId, SubmissionTime) VALUES (?, ?, ?);";
                PreparedStatement insertStmt = db.prepareStatement(insertSubmissionQuery);
                insertStmt.setInt(1, rs1.getInt("UserId"));
                insertStmt.setInt(2, rs2.getInt("ExerciseId"));
                insertStmt.setLong(3, submission.submissionTime.getTime());
        
                insertStmt.executeUpdate();
        
                // Retrieve the generated ID
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            } 
            else {
                // Update existing submission
                String updateSubmissionQuery = "UPDATE Submission SET UserId = ?, ExerciseId = ?,SubmissionTime = ? WHERE SubmissionId = ?;";
                PreparedStatement updateStmt = db.prepareStatement(updateSubmissionQuery);
                updateStmt.setInt(1, rs1.getInt("UserId"));
                updateStmt.setInt(2, rs2.getInt("ExerciseId"));
                updateStmt.setLong(3, submission.submissionTime.getTime());
                updateStmt.setInt(4, submission.id);
        
                updateStmt.executeUpdate();
        
                return submission.id;
            }
         }
        return -1;
    }


    // ============= Submission Query ===============


    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the latest submission for the given exercise by the given user.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getLastSubmission(User, Exercise)}
     *
     * @return
     */
    PreparedStatement getLastSubmissionGradesStatement() throws SQLException {
        // TODO: Implement
        return null;
    }

    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the <i>best</i> submission for the given exercise by the given user.
     * The best submission is the one whose point total is maximal.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getBestSubmission(User, Exercise)}
     *
     */
    PreparedStatement getBestSubmissionGradesStatement() throws SQLException {
        // TODO: Implement
        return null;
    }

    /**
     * Return a submission for the given exercise by the given user that satisfies
     * some condition (as defined by an SQL prepared statement).
     * <p>
     * The prepared statement should accept the user name as parameter 1, the exercise id as parameter 2 and a limit on the
     * number of rows returned as parameter 3, and return a row for each question corresponding to the submission, sorted by questionId.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @param stmt
     * @return
     * @throws SQLException
     */
    Submission getSubmission(User user, Exercise exercise, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, user.username);
        stmt.setInt(2, exercise.id);
        stmt.setInt(3, exercise.questions.size());

        ResultSet res = stmt.executeQuery();

        boolean hasNext = res.next();
        if (!hasNext)
            return null;

        int sid = res.getInt("SubmissionId");
        Date submissionTime = new Date(res.getLong("SubmissionTime"));

        float[] grades = new float[exercise.questions.size()];

        for (int i = 0; hasNext; ++i, hasNext = res.next()) {
            grades[i] = res.getFloat("Grade");
        }

        return new Submission(sid, user, exercise, submissionTime, (float[]) grades);
    }

    /**
     * Return the latest submission for the given exercise by the given user.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @return
     * @throws SQLException
     */
    public Submission getLastSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getLastSubmissionGradesStatement());
    }


    /**
     * Return the submission with the highest total grade
     *
     * @param user the user for which we retrieve the best submission
     * @param exercise the exercise for which we retrieve the best submission
     * @return
     * @throws SQLException
     */
    public Submission getBestSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getBestSubmissionGradesStatement());
    }
}
