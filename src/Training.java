//Avnoor Ludhar
//Training.java
//This is a small tool for coaches to create workouts on. The whole idea of the program is to be able to create workouts for many people in
//an efficient way. This tool has many features, you can add more clients to the program, you can delete them. You can save workouts and create them,
//there's a feature known as the exercise library which holds all the exercises that have been put in by the user. These are the list of exercises that
//can be used. The program is the base for an efficient way to create workout plans.


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;


//Main class to run the frame.
public class Training extends JFrame {
     TrainingTool game= new TrainingTool();

    public Training(){
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        add(game);
        pack();    // set the size of my Frame exactly big enough to hold the contents.
        setVisible(true);


        addWindowListener(new java.awt.event.WindowAdapter(){
            //Writing out to file when the window closes.
            public void windowClosing(java.awt.event.WindowEvent e){

                game.saveInfo();


            System.exit(EXIT_ON_CLOSE);
        }
    });
}


    public static void main(String[] args){
        Training frame = new Training();
    }
}


//This is the main panel for the program. This contains all the screens that belong to the main screen. It also contains all the elements that
//belong to the game including the clients, and exercise library. All of the main methods are called here.

class TrainingTool extends JPanel implements KeyListener, ActionListener, MouseListener {

    //screens that belong to the main class
    private int screen;
    private int screen2;
    public final int INTRO = 0, EXERCISECREATE = 1, EXERCISELIBRARY = 2, EXERCISE = 3, CLIENT = 4, NEWCLIENT = 5;
    //fonts
    private final Font font, font2;

    private Timer timer;
    //Images
    private Image title, clean, curls;
    //mouse position
    private int mx;
    private int my;
    //keys that are pressed
    private boolean []keys;

    //Rectangles for the creation of exercises, clients, and to see the exercise library.
    private Rectangle exerciseCreator, newClientRect, exerciseLibRect;

    //All the different clients on the screen.
    private ArrayList<Client> clients;
    //All the exercises in the Exercise library.
    private ArrayList<Exercise> exerciseLibrary;

    //Checks the exercise clicked in the exercise library to draw the content for that exercise.
    private Exercise currentExercise;

    //This holds all the raw information that I had in the text files.
    private static TextFileInfo information;
    //Contains all the workouts that have been saved.
    private ArrayList<Workout> savedWorkouts;
    //Indexes of the current client that's being modified and the current saved workout that's being used.
    private int currentSavedWorkout, currentClient;
    //Objects for exercises and clients being added by the user.
    private TypingBox newExerciseText, newClientText;

    //Checks to make sure I only change information once and not continously.
    private boolean informationCheck;
    //Animations for the graphics on the screen.
    private Animation runningAnimation, jumpRopeAnimation;


    public TrainingTool(){

        setPreferredSize(new Dimension(1440, 792));
        setFocusable(true);
        requestFocus();
        addMouseListener(this);
        addKeyListener(this);

        keys = new boolean[KeyEvent.KEY_LAST+1];
        //Sets all the rectangles to where the text is drawn on the screen.
        exerciseLibRect = new Rectangle(725, 480, 340, 50);
        newClientRect = new Rectangle(225, 480, 340, 50);
        exerciseCreator = new Rectangle(350, 600, 650,50);

        screen = INTRO;
        screen2 = INTRO;
        font = new Font("Futura", Font.BOLD, 45);
        font2 = new Font("Futura", Font.BOLD, 80);
        title = new ImageIcon("beach.png").getImage();
        clean = new ImageIcon("clean.gif").getImage();
        curls = new ImageIcon("curls.gif").getImage();

        information = new TextFileInfo();
        //splits info into the exercises to be put into the exercise library.
        exerciseLibrary = information.splitInfo();
        savedWorkouts = information.getWorkouts(exerciseLibrary);
        //Splits the information into the different clients and returns it.
        clients = information.splitInfo2();
        loadClientWorkouts();

        //Loads the rectangles for each saved workout to check collisions with the position of the text on the screen.
        savedWorkouts = Workout.loadSavedWorkoutRects(savedWorkouts);

        //sets the values for the indexes to something that isn't used.
        currentSavedWorkout = 0;
        currentClient = -1;

        newExerciseText = new TypingBox();
        newClientText = new TypingBox();
        informationCheck = false;
        runningAnimation = new Animation(Animation.loadRunningImages());
        jumpRopeAnimation = new Animation(Animation.loadJumpRopeImages());

        timer = new Timer(20, this);
        timer.start();
    }


    //gets the raw data from the class.
    public static TextFileInfo getInformation() {
        return information;
    }

    //Gets all the workouts from each person in order from the text file then sets them to each client.
    public void loadClientWorkouts(){
        ArrayList<Workout[]> allWeeks = information.getWorkouts2(exerciseLibrary);
        for(int i = 0; i<clients.size(); i++){
            clients.get(i).setWeek(allWeeks.get(i));
        }
    }


    //Saves info back to the file by cycling through the arraylist and printing out to the text files.
    public void saveInfo(){
        try {
            PrintWriter outFile = new PrintWriter(
                    new BufferedWriter(new FileWriter("training.txt")));

            PrintWriter outFile2 = new PrintWriter(
                    new BufferedWriter(new FileWriter("savedWorkouts.txt")));

            for (int i = 0; i < information.getExerciseLibrary().size(); i++) {
                outFile.println(information.getExerciseLibrary().get(i));
            }

            outFile.println("");
            outFile.close();
            saveInfo(outFile2);
            saveInfo2();

        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    //saves info back to the text file that contains the saved workouts.
    public void saveInfo(PrintWriter outFile){
        try{
            //loops through all the elements in the saved workouts arraylist.
            for (int i = 0; i < savedWorkouts.size(); i++) {
                if(savedWorkouts.get(i) == null){
                    break;
                }
                if(savedWorkouts.get(i).getWorkout().size() == 0) {
                    outFile.println(savedWorkouts.get(i).getDay() + ";");
                }else{
                    outFile.print(savedWorkouts.get(i).getDay() + ";");
                }
                //Prints all the values of the workout that are needed. Like the name, id, sets, reps, and extra info like weight to the file. Separated by
                //semi-colons. When a exercise is finished it then moves to the next line.
                for(int e = 0; e<savedWorkouts.get(i).getWorkout().size(); e++){
                    outFile.print(savedWorkouts.get(i).getWorkout().get(e).getID() + ";");
                    outFile.print(savedWorkouts.get(i).getWorkout().get(e).getName() + ";");
                    outFile.print(savedWorkouts.get(i).getWorkout().get(e).getSets() + ";");
                    outFile.print(savedWorkouts.get(i).getWorkout().get(e).getReps() + ";");
                    if(e == savedWorkouts.get(i).getWorkout().size()-1){
                        outFile.println(savedWorkouts.get(i).getWorkout().get(e).getExtra());
                    }else{
                        outFile.print(savedWorkouts.get(i).getWorkout().get(e).getExtra() + ";");
                    }
                }
            }
            outFile.close();
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    //Saves all the info to the text file that has to do with the clients and the workouts that go with it.
    public void saveInfo2(){
        try{
            PrintWriter outFile = new PrintWriter(
                    new BufferedWriter(new FileWriter("allTraining.txt")));
            //loops through all the clients.
            for (int w = 0; w < clients.size(); w++) {
                //gets the week of workouts
                Workout [] workout = clients.get(w).getWeek();
                //prints the name on a line
                outFile.println(clients.get(w).getClientName());
                //Then looks through the workout week and prints to the file the same way as in the save info method.
                for(int i = 0; i<workout.length; i++){
                    if(workout[i] == null){
                        break;
                    }
                    if(workout[i].getWorkout().size() == 0) {
                        outFile.println(workout[i].getDay() + ";");
                    }else{
                        outFile.print(workout[i].getDay() + ";");
                    }
                    for(int e = 0; e<workout[i].getWorkout().size(); e++){
                        outFile.print(workout[i].getWorkout().get(e).getID() + ";");
                        outFile.print(workout[i].getWorkout().get(e).getName() + ";");
                        outFile.print(workout[i].getWorkout().get(e).getSets() + ";");
                        outFile.print(workout[i].getWorkout().get(e).getReps() + ";");
                        if(e == workout[i].getWorkout().size()-1){
                            outFile.println(workout[i].getWorkout().get(e).getExtra());
                        }else{
                            outFile.print(workout[i].getWorkout().get(e).getExtra() + ";");
                        }
                    }
                }
            }
            outFile.close();
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        informationChange();
    }


    //Calls all typingBox objects methods to get the key inputs according to the screen it is on. Including the one in the client class.
    @Override
    public void keyTyped(KeyEvent e) {
        if(screen == EXERCISECREATE){
            information = newExerciseText.keyTyping(e);
        }
        else if(screen == NEWCLIENT){
            newClientText.keyTyping3(e);
        }
        for(int i = 0; i<clients.size(); i++){
            if(clients.get(i).getScreen() == Client.SETS){
                clients.get(i).getSetsReps().keyTyping2(e);
            }
        }

    }


    //gets the key code all objects to get the key inputs according to the screen it is on.
    @Override
    public void keyPressed(KeyEvent e) {
        if(screen == EXERCISECREATE){
            newExerciseText.getKeyCode(e);
        }else if(screen == NEWCLIENT){
            newClientText.getKeyCode(e);
        }
        for(int i = 0; i<clients.size(); i++){
            if(clients.get(i).getScreen() == Client.SETS){
                clients.get(i).getSetsReps().getKeyCode(e);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    //Sets the mouse position to make it easier to access as a variable and calls all the mouseCollisions with rectangles and code that goes with it.
    @Override
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
        mouseCollisions(e);
    }

    @Override
    public void mouseReleased(MouseEvent e){

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //checks all rectangles in the main screen to see if the mouse hit it and changes the screens accordingly.
    public void mouseCollisions(MouseEvent e){

        if(screen == INTRO){
            //Checks if any of the clients rectangles on the screen are hit. Sets the screen to CLIENT and gets the idex of the current client
            //being modified
            for(int i = 0; i<clients.size(); i++){
                if(clients.get(i).getMainScreenRectangle().contains(mx,my)){
                    screen = CLIENT;
                    currentClient = i;
                }
            }
            if(exerciseCreator.contains(mx, my)){
                screen = EXERCISECREATE;
            }
            else if(exerciseLibRect.contains(mx, my)){
                screen = EXERCISELIBRARY;
            }else if(newClientRect.contains(mx,my)){
                screen = NEWCLIENT;
            }

        }else if (screen == CLIENT){
            changeClient(currentClient,e);
        }

        else if(screen == EXERCISECREATE){
            if(new Rectangle(50, 700, 175, 50).contains(e.getX(),e.getY())){    //normal back button
                screen = INTRO;
            }
        }
        else if(screen == EXERCISELIBRARY) {
            if (screen2 == INTRO) {
                if (new Rectangle(50, 700, 175, 50).contains(mx, my)) {
                    screen = INTRO;
                }
                else if (Exercise.checkExerciseRect(exerciseLibrary, mx, my)) {                    //If in the exercise library if you click an exercise it changes the second screen to EXERCISE so the exercise can be drawn.
                    currentExercise = Exercise.getExercise(exerciseLibrary, e.getX(), e.getY());
                    screen2 = EXERCISE;
                }
            }
            else if (screen2 == EXERCISE) {     //normal back button.
                currentExercise.playVideo(getMousePosition());
                if (new Rectangle(50, 700, 175, 50).contains(mx, my)) {
                    screen2 = INTRO;
                }
            }
        }
        else if(screen == NEWCLIENT){
            if (new Rectangle(50, 700, 175, 50).contains(mx, my)) {    //normal back button
                screen = INTRO;
                newClientText.setClientName("");
            }
            if(new Rectangle(1100, 700, 275, 50).contains(getMousePosition())){    //Confirm button to add the client.
                clients.add(new Client(newClientText.getClientName()));
                newClientText.setClientName("");
                screen = INTRO;
            }
        }
    }

    //Checks if saved workouts are greater than 1 to make sure the user can't spam the same workout into the arraylist. Adds the current workout of the client.
    public void saveWorkout(Client client) {
        if (savedWorkouts.size() > 1) {
            if (client.getCurrentWorkout() != savedWorkouts.get(savedWorkouts.size() - 1) && client.getCurrentWorkout().getWorkout().size() > 0) {
                savedWorkouts.add(client.getCurrentWorkout());
            }
        }else{
            savedWorkouts.add(client.getCurrentWorkout());
        }
    }

    //Changes the current client according to certain things that can only be accessed in the main class.
    public void changeClient(int i, MouseEvent e){
        if(clients.get(currentClient).getScreen() == Client.MAINSCREEN){    //changes the main classes screen to INTRO if the screen in the client is MAINSCREEN.
            if(new Rectangle(50, 700, 175, 50).contains(e.getX(),e.getY())){
                screen = INTRO;
            }else if(clients.get(currentClient).getDeleteClientRect().contains(mx,my)){    //checks the delete rectangle in the client to see if the client is being deleted.
                screen = INTRO;
                clients.remove(currentClient);
                return;
            }
        }
        clients.get(i).changeClientScreen(exerciseLibrary,mx,my);
        if(clients.get(i).getScreen() == Client.EXERCISES){
            clients.get(i).loadChecks(exerciseLibrary,getGraphics());    //If the screen in the client is EXERCISES it loads the checkMarks to the client.
        }else if(clients.get(i).getScreen() == Client.WORKOUT){
            if(new Rectangle(300, 700, 370, 50).contains(getMousePosition())){
                saveWorkout(clients.get(i));    //checks the rectangle that is around the save workout button and saved the current workout.
                savedWorkouts = Workout.loadSavedWorkoutRects(savedWorkouts);    //loads the rectangles for the saved workouts again to load the new rectangle for the new saved workout.
            }
        }else if(clients.get(i).getScreen() == Client.SAVEDWORKOUTS){    //if the screen is SAVEDWORKOUTS in the client checks to see if the mouse is collided with the saved workouts rectangles.
            currentSavedWorkout = Workout.savedWorkoutCollisions(getMousePosition(),savedWorkouts,clients.get(i));
        }else if(clients.get(i).getScreen() == Client.SAVEDWORKOUT){
            if(new Rectangle(1100, 700, 200, 50).contains(getMousePosition())){    //Checks to see if the rectangle around confirm is hit in the SAVEDWORKOUT screen.
                Workout tmpWorkout = new Workout(clients.get(i).getCurrentWorkout().getDay());    //Creates a temporary workout of the current clients day that was being changes.
                tmpWorkout.setSavedWorkoutRectangle(savedWorkouts.get(currentSavedWorkout).getSavedWorkoutRectangle());    //Sets the rectangle to that saved workouts rectangle.
                tmpWorkout.setWorkout(savedWorkouts.get(currentSavedWorkout).getWorkout());    //Saves the workout.
                clients.get(i).setCurrentWorkout(tmpWorkout);    //It then sets the workout to the current workout being changes. This makes sure that multiple workouts of the week can have the same saved workout.
                clients.get(i).resetHexagons();    //Resets the hexagons in the creation of the workout.
                clients.get(i).setScreen(Client.MAINSCREEN);    //sets the screen.
            }
            else if(new Rectangle(600, 700, 175, 50).contains(getMousePosition())){    //checks the delete button and deletes that saved workout.
                savedWorkouts.remove(currentSavedWorkout);
                clients.get(i).setScreen(Client.SAVEDWORKOUTS);
            }
        }
    }


    //Changes the information according to the info inputted into the text box at that screen.
    public void informationChange(){
        if(screen == INTRO){
            if(informationCheck) {
                //sets the exercise in the exercise library then resets everything and turns informationcheck false.
                information.setExerciseLibrary(Exercise.addExercise(information.getExerciseLibrary(), newExerciseText.getEntries())); //entries used to be here
                exerciseLibrary = information.splitInfo();
                newExerciseText.setEntries(new ArrayList<String>());
            }
            informationCheck = false;
        }else if(screen == EXERCISECREATE){
            informationCheck = true;
        }
    }


    //Loads the screens dimensions to fill the screen. Useless since screen sized is now fixed to my macs screen size.
    public void loadScreenDims(){
        for(int i = 0; i< clients.size(); i++){
            clients.get(i).setScreenHeight(getHeight());
            clients.get(i).setScreenWidth(getWidth());
        }
    }

    //All the drawing in the program according to the screen.
    public void paint(Graphics g){
        loadScreenDims();
        setClientRects(g);
        if(screen == INTRO){
            introDraw(g);
        }else if(screen == CLIENT){
            exerciseLibrary = Exercise.exerciseLibraryRectLoad(exerciseLibrary,g);    //Loads all the rectangles in the exercise library to be checked when creating a workout.
            clients.get(currentClient).drawClient(g,getMousePosition(), exerciseLibrary, jumpRopeAnimation, clean, savedWorkouts);    //draws the current client.
            if(clients.get(currentClient).getScreen() == Client.SAVEDWORKOUT){    //draws the SAVEDWORKOUT screen.
                if(currentSavedWorkout != -1){
                    savedWorkouts.get(currentSavedWorkout).drawSavedWorkout(currentSavedWorkout,g, getWidth(),getHeight(),getMousePosition());
                }
            }
        }

        else if(screen == EXERCISECREATE){
            newExerciseText.newExerciseDraw(g,getMousePosition(),getWidth(),getHeight());    //Draws the screen that creates an exercise.
            runningAnimation.moveImages(g,200,275);
        }
        else if(screen == NEWCLIENT){
            newClientText.newClientdraw(g,getMousePosition(), getWidth(),getHeight(), curls);
            runningAnimation.moveImages(g,200,275);
        }
        else if(screen == EXERCISELIBRARY){
            if(screen2 == INTRO){    //Checks the second screen which is for the exercise library to differentiate the exercise library and an exercise.(done early in the code)
                //loads all the rectangles onto the screen.
                exerciseLibrary = Exercise.exerciseLibraryRectLoad(exerciseLibrary,g);
                Exercise.exerciseLibraryDraw(exerciseLibrary,g,getMousePosition(),getWidth(),getHeight());
                backHover(g);
            }
            else if(screen2 == EXERCISE){
                Exercise.paintExercise(g,currentExercise, getMousePosition(), getWidth(),getHeight(),clean);
                backHover(g);
            }
        }
    }

    //Draws the hover for all the back buttons.
    public void backHover(Graphics g){
        g.setColor(Color.black);
        TrainingTool.checkRect(g,new Rectangle(50,700,175,50),getMousePosition(),Color.red);

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("BACK", 50,750);
    }

    //Sets the rectangles for the clients on the mainscreen.
    public void setClientRects(Graphics g){
        int counter = 0;
        g.setFont(font);
        for(int y = 200; y<=500; y+= 100){
            for(int x = 200; x<=1100; x+=300){
                if(counter == clients.size()){
                    break;
                }
                clients.get(counter).setMainScreenRectangle(new Rectangle(x,y,g.getFontMetrics().stringWidth(clients.get(counter).getClientName()),50));
                counter += 1;
            }
        }
    }

    //Draws everything in the INTRO screen.
    public void introDraw(Graphics g){
        g.drawImage(title, 0,-100,this);

        int counter = 0;
        g.setColor(Color.black);
        g.setFont(font2);
        g.drawString("Training tool", 450, 150);
        g.setFont(font);
        for(int y = 250; y<=550; y+= 100){    //Loops in a grid pattern and draws the clients at the positions.
            for(int x = 200; x<=1100; x+=300){
                if(counter == clients.size()){
                    break;
                }
                TrainingTool.checkRect(g,clients.get(counter).getMainScreenRectangle(),getMousePosition(),Color.green);
                g.drawString(clients.get(counter).getClientName(),x,y);
                counter += 1;
            }
        }

        TrainingTool.checkRect(g,exerciseLibRect,getMousePosition(),Color.green);
        g.drawString("Exercise Library", 725,530);

        TrainingTool.checkRect(g,newClientRect,getMousePosition(),Color.green);
        g.drawString("Add Client", 325,530);

        TrainingTool.checkRect(g,exerciseCreator,getMousePosition(),Color.green);
        g.drawString("Add a new exercise to library!", 350,650);


    }

    //Does the hover for all rectangles in the code is static so any class can access it.
    public static void checkRect(Graphics g,Rectangle rect, Point mouse, Color colour){
        g.setColor(Color.black);
        try{
            if(rect.contains(mouse)){
                g.setColor(colour);
            }

        }catch(Exception e){
        }
    }

}

//Workout class draws out all the information in the workout and contains all the information that would be in the workout.
class Workout{
    private ArrayList<Exercise> workout;    //All the exercises in the workout.
    private String day;    //the day of the week
    private Rectangle savedWorkoutRectangle;    //The rectangle to check the saved workouts.

    private static int [] xPos = {50,255,460,665,870,1075,1280};    //Is a static array corresponding to the days of the week to draw out all the workouts of the client for the week.

    public Workout(String day){
        workout = new ArrayList<Exercise>();
        this.day = day;
        savedWorkoutRectangle = null;
    }

    //Special constructor that takes in the exercise library and a string from a text file and breaks it up according to the semi colons to create a workout.
    public Workout(String record, ArrayList<Exercise> ex){
        String [] record1 = record.split(";");
        day = record1[0];
        workout = new ArrayList<Exercise>();
        int counter = 3;

        for(int i = 1; i<record1.length; i+=5){
            workout.add(findExercise(ex,record1[i]));
        }
        for(int w = 0; w< workout.size(); w++){
            workout.get(w).setSets(record1[counter]);
            counter += 5;
        }
        counter = 4;

        for(int w = 0; w< workout.size(); w++) {
            workout.get(w).setReps(record1[counter]);
            counter +=5;
        }
        counter = 5;
        for(int w = 0; w< workout.size(); w++) {
            if(w == workout.size()-1){
                break;
            }
            workout.get(w).setExtraInfo(record1[counter]);
            counter +=5;

        }
        savedWorkoutRectangle = null;
    }

    public Rectangle getSavedWorkoutRectangle(){
        return savedWorkoutRectangle;
    }

    public void setSavedWorkoutRectangle(Rectangle rectangle){
        savedWorkoutRectangle = rectangle;
    }

    public void setWorkout(ArrayList<Exercise> workout) {
        this.workout = workout;
    }

    public String getDay(){
        return day;
    }

    public void addExercise(Exercise exercise){
        workout.add(exercise);
    }

    public ArrayList<Exercise> getWorkout(){
        return workout;
    }

    //Finds the exercise using the ID number of the exercise in the exercise library.
    public Exercise findExercise(ArrayList<Exercise> exercises, String ID){
        int id = Integer.parseInt(ID);
        for(int e = 0; e<exercises.size(); e++){
            if(exercises.get(e).getID() == id){
                return exercises.get(e);
            }
        }
        return null;
    }

    //Checks when the rectangles of the saved workouts are contacted with the mouse. Made it static because I believe it belongs to the workout class.
    public static int savedWorkoutCollisions(Point mouse, ArrayList<Workout> savedWorkouts, Client client){
        for(int i = 0; i<savedWorkouts.size(); i++) {
            if (savedWorkouts.get(i).getSavedWorkoutRectangle().contains(mouse)) {
                savedWorkouts.get(i).day = client.getCurrentWorkout().day;
                client.setScreen(Client.SAVEDWORKOUT);
                return i;
            }
        }
        return -1;
    }

    //draws the create workout screen.
    public static void createWorkoutDraw(ArrayList<Exercise> exercises, ArrayList<CheckMark> checks, Graphics g, Point pos, int width, int height){
        g.setColor(new Color(137,207,240));
        g.fillRect(0,0,width,height);
        g.setFont(new Font("Futura", Font.PLAIN, 80));
        g.setColor(Color.black);
        g.drawString("Create new Workout", 400,120);

        chooseExercises(exercises,checks, g,pos);
    }

    //Loads the rectangles for the saved workout screen. By using a nested for loop and looping through positions
    public static ArrayList<Workout> loadSavedWorkoutRects(ArrayList<Workout> savedWorkouts){
        int counter = 0;
        for(int x = 200; x<1400; x+= 100){
            for(int y = 150; y<700; y+=50){
                if(counter == savedWorkouts.size()){
                    break;
                }
                savedWorkouts.get(counter).setSavedWorkoutRectangle(new Rectangle(x,y,80,35));
                counter += 1;
            }
        }
        return savedWorkouts;
    }

    //draws all the saved workouts. With the same nested for loop as the rectangles.
    public static void drawSavedWorkoutScreen(ArrayList<Workout> savedWorkouts, Graphics g, Point mouse, int width, int height){
        g.setColor(new Color(96, 189, 96));
        g.fillRect(0,0,width,height);
        g.setFont(new Font("Futura", Font.BOLD, 100));
        g.setColor(Color.black);
        g.drawString("Saved Workouts", 750 - g.getFontMetrics().stringWidth("Saved Workouts")/2,110);

        g.setFont(new Font("Futura", Font.BOLD, 35));
        int counter = 0;
        for(int x = 200; x<1400; x+=100){
            for(int y = 185; y<700; y+=50){
                if(counter == savedWorkouts.size()){
                    break;
                }
                int counter2 = counter +1;
                TrainingTool.checkRect(g,savedWorkouts.get(counter).getSavedWorkoutRectangle(),mouse,Color.blue);

                g.drawString("W" + counter2, x,y);
                counter += 1;
            }
        }
        g.setFont(new Font("Futura", Font.BOLD, 50));
        TrainingTool.checkRect(g,new Rectangle(50, 700, 175, 50),mouse,Color.red);
        g.drawString("BACK", 50,750);
    }

    //draws the current saved workout. Gets the index of the saved workouts to get the number of the saved workout.
    public void drawSavedWorkout(int workoutNum,Graphics g, int width, int height, Point mouse){
        workoutNum ++;
        g.setColor(new Color(137,207,240));
        g.fillRect( 0,0,width,height);
        g.setColor(Color.black);
        g.setFont(new Font("Futura", Font.BOLD, 100));
        g.drawString("Saved Workout #" + workoutNum, 750 - g.getFontMetrics().stringWidth("Saved Workouts #" + workoutNum)/2,110);

        g.setFont(new Font("Futura", Font.BOLD, 30));

        int y = 140;

        for(int i = 0; i<workout.size(); i++){    //draws all the exercises contained in the workout.
            y += 45;
            int x = 300;
            g.drawString(workout.get(i).getName() +":",x,y);
            x += 50+g.getFontMetrics().stringWidth(workout.get(i).getName());
            g.drawString(workout.get(i).getSets() + "X" + workout.get(i).getReps(),x,y);
            g.drawString("Extra info:  " + workout.get(i).getExtra(),x+35+g.getFontMetrics().stringWidth(workout.get(i).getSets() + "X" + workout.get(i).getReps()),y);
        }

        g.setFont(new Font("Futura", Font.BOLD, 50));
        TrainingTool.checkRect(g,new Rectangle(50, 700, 175, 50),mouse,Color.red);

        g.drawString("BACK", 50,750);

        TrainingTool.checkRect(g,new Rectangle(1100, 700, 175, 50),mouse,Color.green);

        g.drawString("CONFIRM", 1100,750);

        TrainingTool.checkRect(g,new Rectangle(600, 700, 175, 50),mouse,Color.red);
        g.drawString("DELETE", 600,750);

    }

    //Draws the workout on the screen in the main screen of the client.
    public void drawWorkout(Graphics g){
        String [] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        int x = 0;

        //Sets x according to what day the workout that was called on was.
        for(int d = 0; d<days.length; d++){
            if(days[d].equals(day)){
                x = Workout.xPos[d];
            }
        }

        g.setFont(new Font("Futura",Font.PLAIN,28));
        g.drawString(day,x-20 ,150);
        g.drawLine(x-35,155,x+8+g.getFontMetrics().stringWidth(day),155);
        int counter = 0;

        //Cycles through each exercise in the workout and draws it under the day using a for loop.
        for(int y = 175; y<720; y+=45){
            if(counter == workout.size()){
                break;
            }
            g.setFont(new Font("Futura",Font.PLAIN,14));
            g.drawString(workout.get(counter).getName()+":",x-25,y);
            g.drawString(workout.get(counter).getSets() + "X" + workout.get(counter).getReps(),x-20 +g.getFontMetrics().stringWidth(workout.get(counter).getName()),y);
            g.setFont(new Font("Futura",Font.PLAIN,14));
            g.drawString(workout.get(counter).getExtra(),x-30,y+18);
            counter ++;
        }
    }

    //Draws the screen with all the exercises in the library and includes all the checkmarks next to them.
    public static void chooseExercises(ArrayList<Exercise> exercises,ArrayList<CheckMark> checks, Graphics g, Point pos) {
        g.setFont(new Font("Futura", Font.PLAIN, 25));
        int counter = 0;

        for (int x = 100; x <= 1400; x += 260) {    //loops through all the positions on the screen and prints the name and the checkmark according to the counter.
            for (int y = 200; y <= 700; y += 40) {
                if (counter >= exercises.size()) {
                    break;
                }
                g.setColor(Color.black);
                try {
                    if (exercises.get(counter).getexerciseRect().contains(pos)) {
                        g.setColor(Color.red);

                    }

                } catch (Exception e) {
                }

                g.drawString(exercises.get(counter).getName(), x, y);

                checks.get(counter).drawCheckmark(g,pos);
                counter += 1;
            }
        }
    }
}

//Contains all the methods and information that are related to a singular exercise. Such as a name, description, etc.
class Exercise{
    private int ID;    //ID of the Exercise
    private String Name;    //name
    private String Description;    //Description of the exercise.
    private String URL;

    private Rectangle exerciseRect;    //Rectangle to be clicked when you want to see the information in the exercise.
    private static Font font = new Font("Futura", Font.PLAIN, 70);
    private static Font font2 = new Font("Futura", Font.PLAIN, 20);

    //sets and reps and extra info like 3 seconds down or the weight.
    private int sets;
    private int reps;
    private String extraInfo;

    private static final Rectangle LinkRectangle = new Rectangle(1000,700,350,50);    //Rectangle for the youtube link for the exercise.

    //Takes in a record from the raw information and creates a record from it.
    public Exercise(String exercise){
        String []record = exercise.split(";");
        ID = Integer.parseInt(record[0]);
        Name = record[1];
        Description = record[2];
        try {
            URL = record[3];
        }catch(Exception e){
            URL = " ";
        }
        exerciseRect = null;
        sets = 0;
        reps = 0;
        extraInfo = "";

    }

    //Another constructor to duplicate exercises.
    public Exercise(Exercise e){
        ID = e.ID;
        Name = e.Name;
        Description = e.Description;
        URL = e.URL;
        exerciseRect = e.exerciseRect;
        sets = 0;
        reps = 0;
        extraInfo = "";
    }

    public void setSets(String s){
        sets = Integer.parseInt(s);
    }
    public void setReps(String s){
        reps = Integer.parseInt(s);
    }

    public String getExtra(){
        return extraInfo;
    }
    public int getReps(){
        return reps;
    }

    public int getSets(){
        return sets;
    }

    public void setExtraInfo(String s){
        if(s.equals("")){
            extraInfo = " ";
        }else{
            extraInfo = s;
        }

    }

    public void setExerciseRect(Rectangle rect){
        exerciseRect = rect;
    }


    public Rectangle getexerciseRect(){
        return exerciseRect;
    }

    public String getName(){
        return Name;
    }

    public static Rectangle getLinkRectangle(){
        return LinkRectangle;
    }

    public int getID(){
        return ID;
    }


    //Static method to add to the exercise library.
    public static ArrayList<String> addExercise(ArrayList<String> exerciseLibaray, ArrayList<String> newRecords){

        for(int i = 0; i< newRecords.size(); i++){
            exerciseLibaray.add(newRecords.get(i));
        }

        return exerciseLibaray;
    }

    //Draws the exercise with the description, makes sure it is in a neat pattern.
    public static void paintExercise(Graphics g, Exercise exercise, Point mouse, int width, int height, Image clean){
        ArrayList<String> description = new ArrayList<>();
        int count = 0;

        g.setColor(new Color(247, 247, 247, 255));
        g.fillRect(0,0,width,height);
        g.drawImage(clean,710,160,null);
        g.setColor(Color.black);
        g.setFont(font);
        g.drawString(exercise.Name, 720-g.getFontMetrics().stringWidth(exercise.Name)/2, 100);

        g.setFont(font2);
        int descWidth = g.getFontMetrics().stringWidth(exercise.Description);

        //Checks the width of the description and breaks it into multiple strings accordingly.
        if(descWidth>500 && descWidth<1000){
             description.add(exercise.Description.substring(0,exercise.Description.length()/2)) ;
             description.add(exercise.Description.substring(exercise.Description.length()/2)) ;
        }
        if(descWidth>1000 && descWidth<1500){
            description.add(exercise.Description.substring(0,exercise.Description.length()/3));
            description.add(exercise.Description.substring(exercise.Description.length()/3, exercise.Description.length()*2/3)) ;
            description.add(exercise.Description.substring(exercise.Description.length()* 2/3));
        }
        if(descWidth>1500){
            description.add(exercise.Description.substring(0,exercise.Description.length()/4));
            description.add(exercise.Description.substring(exercise.Description.length()/4, exercise.Description.length()*2/4));
            description.add(exercise.Description.substring(exercise.Description.length()*2/4, exercise.Description.length()*3/4));
            description.add(exercise.Description.substring(exercise.Description.length()* 3/4));
        }
        if(descWidth <500){
            description.add(exercise.Description);
        }

        g.setColor(Color.black);
        for(int y = 150; y<=350; y+= 50){
            if(count == description.size()){
                break;
            }
            g.drawString(description.get(count), 200, y +20);
            count +=1;
        }

        g.setFont(new Font("Futura", Font.BOLD, 50));
        TrainingTool.checkRect(g,LinkRectangle,mouse,Color.blue);

        g.drawString("Click for vid!", 1000,750);

    }

    //Draws out the exercise library and makes the hover.
    public static void exerciseLibraryDraw(ArrayList<Exercise> exercises, Graphics g, Point pos, int width, int height) {
        g.setColor(new Color(96, 189, 96));
        g.fillRect(0,0,width,height);
        g.setColor(Color.black);
        g.setFont(new Font("Futura", Font.PLAIN, 80));
        g.drawString("Exercise Library", 400, 120);

        g.setFont(new Font("Futura", Font.PLAIN, 25));
        int counter = 0;

        for (int x = 100; x <= 1400; x += 260) {
            for (int y = 200; y <= 700; y += 40) {
                if (counter >= exercises.size()) {
                    break;
                }
                TrainingTool.checkRect(g,exercises.get(counter).exerciseRect,pos, Color.red);

                g.drawString(exercises.get(counter).Name, x, y);
                counter += 1;
            }
        }
    }


    //Loads the rectangles into the exercises only if they are null to ensure it happens once.
    public static ArrayList<Exercise> exerciseLibraryRectLoad(ArrayList<Exercise> exercises, Graphics g){
        g.setFont(new Font("Futura", Font.PLAIN, 25));
        int width;
        int counter = 0;

        if(exercises.get(counter).exerciseRect == null) {
            for (int x = 100; x <= 1400; x += 260) {
                for (int y = 200; y <= 700; y += 40) {    //Goes in a grid like patter just like the exercises are drawn in the exercise library.
                    if (counter >= exercises.size()) {
                        break;
                    }
                    width = g.getFontMetrics().stringWidth(exercises.get(counter).Name);
                    Rectangle rect = new Rectangle(x,y-25,width,25);
                    if (exercises.get(counter).exerciseRect == null) {
                        exercises.get(counter).setExerciseRect(rect);
                    }
                    counter += 1;
                }
            }
        }
        return exercises;
    }

    //checks collisions between the exercises rectangles and the mouse position.
    public static boolean checkExerciseRect(ArrayList<Exercise> exercises,int x, int y){
        for(int i = 0; i<exercises.size(); i++){
            if(exercises.get(i).exerciseRect.contains(x,y)){
                return true;
            }
        }
        return false;
    }

    //Gets the certain exercise according to if it hits the rectangle.
    public static Exercise getExercise(ArrayList<Exercise> exercises,int x, int y){
        for(int i = 0; i<exercises.size(); i++){
            if(exercises.get(i).exerciseRect.contains(x,y)){
                return exercises.get(i);
            }
        }
        return null;
    }

    //Plays the youtube video if the mouse hits the LinkRectangle
    public void playVideo(Point mouse) {
        if(Exercise.getLinkRectangle().contains(mouse)){
            try {
                YoutubePlay.getLink(URL);
            }
            catch(URISyntaxException e){

            }
        }
    }
    
}

//This is the Client class it does a huge amount of the heavy lifting in this program. It has everything that belongs to the client. Including the screen,
//the week of workouts, etc. Has a lot of the methods that add to the client and calls many methods from the other classes.
class Client{

    private Workout [] week;    //A full week of workouts.

    private int screen;    //the screen the client is on.
    public static final int MAINSCREEN = 0, WORKOUT = 1, EXERCISES = 2, EXERCISEDESC = 3, SETS = 4,WEEKDAY = 5, SAVEDWORKOUTS = 6, SAVEDWORKOUT = 7;

    private static Image barbell = new ImageIcon("verticalbarbell.png").getImage();    //Image of a barbell.


    private String name;    //The clients name

    private static int screenWidth,screenHeight;    //width and height of original screen.
    private boolean dayRectcheck;    //Checks to load the day rectangles in the WEEKDAY screen.

    private final Font font;
    private final Font font2;


    private Rectangle newWorkout,mainScreenRectangle;    //rectangle to check if we are making a new workout.

    private ArrayList<Hexagon> hexagons;   //All the hexagons on WORKOUT screen.

    private Exercise ExerciseDescription;    //Exercise used for the description of the exercise.

    private ArrayList<CheckMark> checks;    //All the checkmarks on the WORKOUT screen.

    private CheckMark largeCheckmark;    //The large check mark that appears when workouts are saved
    private int currentHexagon,currentWorkout, currentExercise, prevExercise;    //Indexes used to change the various arraylists.
    private Workout loadWorkoutIn;    //Used to load the previous workout that existed into the workout being created.

    private TypingBox setsReps;    //The textbox for the sets and reps.
    private Rectangle [] dayRects;    //All the rectangles in the WEEKDAY screen.
    private Rectangle deleteClientRect, loadDaysWorkout;    //rectangles for deleting the client and to load the current workout in.

    public Client(String name){
        this.name = name;
        week = new Workout[7];
        screen = MAINSCREEN;
        font = new Font("Futura", Font.BOLD, 100);
        font2 = new Font("Futura", Font.BOLD, 35);
        newWorkout = new Rectangle(850,700,450,35);
        mainScreenRectangle = new Rectangle();
        hexagons = new ArrayList<Hexagon>();
        loadHexagons();
        checks = new ArrayList<CheckMark>();
        setsReps = new TypingBox();
        currentHexagon = 0;
        largeCheckmark = new CheckMark(400,650,27,"");

        dayRects = new Rectangle[7];
        deleteClientRect = new Rectangle(350, 700, 305, 50);
        loadDaysWorkout = new Rectangle(540,480, 250,40);

        currentWorkout=0;
        currentExercise = 0;
        prevExercise = 0;
        dayRectcheck = true;
    }

    public String getClientName(){
        return name;
    }

    public Rectangle getDeleteClientRect() {
        return deleteClientRect;
    }

    public Rectangle getMainScreenRectangle() {
        return mainScreenRectangle;
    }

    public void setScreenHeight(int height){
        screenHeight = height;
    }

    public void setScreenWidth(int width){
        screenWidth = width;
    }

    public Workout[] getWeek(){
        return week;
    }

    public void setWeek(Workout [] week){
        this.week = week;
    }

    public TypingBox getSetsReps(){
        return setsReps;
    }

    public void setMainScreenRectangle(Rectangle rect){
        mainScreenRectangle = rect;
    }

    public int getScreen(){
        return screen;
    }

    public void setScreen(int i){
        screen = i;
    }
    public Workout getCurrentWorkout(){
        return week[currentWorkout];
    }

    public void setCurrentWorkout(Workout w){
        week[currentWorkout] = w;
    }

    //Resets all the hexagons.
    public void resetHexagons(){
        for(int i = 0; i<hexagons.size(); i++){
            hexagons.get(i).setExercise(null);
            hexagons.get(i).setCheck(false);
        }
    }


    //Loads the hexagons
    public void loadHexagons(){
        int size = 70;
        for(int x = 320; x<1220; x+= 250) {
            for (int y = 350; y <= 750; y += 200) {
                hexagons.add(new Hexagon(x, y, size));
            }
        }
    }

    //Loads the previous workout that was being modified into the hexagons.
    public void loadExercisesIntoHexagon(){
        if(loadWorkoutIn != null) {
            for (int i = 0; i < loadWorkoutIn.getWorkout().size(); i++) {
                hexagons.get(i).setExercise(loadWorkoutIn.getWorkout().get(i));
                hexagons.get(i).setCheck(true);
                week[currentWorkout] = loadWorkoutIn;
            }
        }
    }

    //Loads the rectangles for each day of the week.
    public void loadDayRects(Graphics g){
        String [] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int counter = 0;
        g.setFont(font2);

        for(int x = 150; x<1300; x+=300){
            for(int y = 250; y<800; y+= 300){
                if(counter == 7){
                    break;
                }
                dayRects[counter] = new Rectangle(x,y-35,g.getFontMetrics().stringWidth(days[counter]),35);
                counter ++;
            }
        }
    }

    //Draws everything for the client.
    public void drawClient(Graphics g, Point mouse, ArrayList<Exercise> exercises, Animation jumpRope, Image clean, ArrayList<Workout> savedWorkouts){
        if(screen == MAINSCREEN){
            mainScreendraw(g,mouse);
            if(dayRectcheck){
                loadDayRects(g);
                dayRectcheck = false;
            }

        }else if(screen == WEEKDAY){
            drawWeekday(g,mouse);
            jumpRope.moveImages(g,900,25);
        }
        else if(screen == WORKOUT){
           drawNewWorkout(g,mouse);
        }

        else if(screen == EXERCISES){    //calls the methods to draw all the exercises in the creation of a workout.
            Workout.createWorkoutDraw(exercises,checks,g, mouse, screenWidth,screenHeight);
            TrainingTool.checkRect(g,new Rectangle(50, 700, 175, 50),mouse,Color.red);

            g.setFont(new Font("Futura", Font.BOLD, 50));
            g.drawString("BACK", 50,750);
        }
        else if(screen == EXERCISEDESC){    //Calls all methods for the exercises description.
            g.setColor(Color.black);
            Exercise.paintExercise(g,ExerciseDescription, mouse, screenWidth, screenHeight, clean);
            TrainingTool.checkRect(g,new Rectangle(50,700,175,50),mouse,Color.red);
            g.setFont(new Font("Futura", Font.BOLD, 50));
            g.drawString("BACK", 50,750);
        }
        else if(screen == SETS){
            setsReps.setsRepsDraw(g,mouse, screenWidth, screenHeight);
        }else if(screen == SAVEDWORKOUTS){
            Workout.drawSavedWorkoutScreen(savedWorkouts,g,mouse,screenWidth,screenHeight);
        }
        largeCheckmark.countDown();
    }

    //This method draws all the graphics for a new workout being created.
    public void drawNewWorkout(Graphics g, Point mouse){
        g.setColor(Color.WHITE);
        g.fillRect(0,0,screenWidth,screenHeight);
        g.drawImage(Client.barbell,-20,100, null);
        g.drawImage(Client.barbell,1200,100, null);


        g.setFont(new Font("Futura", Font.BOLD, 60));
        g.setColor(Color.black);
        String title = String.format("New Workout for %s", name);
        g.drawString(title, 300,100);

        g.setFont(new Font("Futura", Font.BOLD, 50));

        TrainingTool.checkRect(g,new Rectangle(50, 700, 175, 50),mouse,Color.red);

        g.drawString("BACK", 50,750);
        g.setFont(new Font("Futura", Font.BOLD, 50));

        TrainingTool.checkRect(g,new Rectangle(300, 700, 370, 50),mouse,Color.blue);
        g.drawString("Save workout",300, 750);

        TrainingTool.checkRect(g,new Rectangle(800, 700, 500, 50),mouse,Color.blue);

        g.drawString("Get a saved workout",800, 750);
        g.setColor(Color.BLACK);
        drawHexagons(g, mouse);
        largeCheckmark.drawCheckmark(g);

        g.setFont(new Font("Futura", Font.BOLD, 40));
        TrainingTool.checkRect(g,loadDaysWorkout,mouse,Color.blue);
        g.drawString("load Workout", 540,520);
    }

    //draws each hexagon.
    public void drawHexagons(Graphics g, Point mouse){
        for(int i = 0; i<hexagons.size(); i++){
            hexagons.get(i).drawHexagon(g, mouse);
        }
    }

    //draws everything for the weekday screen.
    public void drawWeekday(Graphics g, Point mouse){

        g.setColor(new Color(96, 189, 96));
        g.fillRect(0,0,screenWidth,screenHeight);

        g.setColor(Color.black);
        g.setFont(font);
        g.drawString("Select Weekday.", 250,100);

        String [] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int counter = 0;
        g.setFont(font2);


        for(int x = 150; x<1300; x+=300){
            for(int y = 250; y<800; y+= 300){
                if(counter == 7){
                    break;
                }
                TrainingTool.checkRect(g,dayRects[counter],mouse,Color.blue);
                g.drawString(days[counter],x, y);
                counter ++;
            }
        }

        TrainingTool.checkRect(g,new Rectangle(50,700,175,50),mouse,Color.red);
        g.setFont(new Font("Futura", Font.BOLD, 50));
        g.drawString("BACK", 50,750);
    }

    //Draws everything on the main screen.
    public void mainScreendraw(Graphics g, Point mouse){
        g.setColor(new Color(137,207,240));
        g.fillRect(0,0,screenWidth,screenHeight);
        g.setColor(Color.black);
        for(int i = 0; i<week.length; i++){
            if(week[i]!= null){
                week[i].drawWorkout(g);
            }
        }

        g.setFont(font);
        g.drawString(name, 400,120);
        g.setFont(font2);
        g.setColor(Color.black);
        TrainingTool.checkRect(g,newWorkout,mouse,Color.green);
        g.drawString("CREATE A NEW WORKOUT", 850,735);

        TrainingTool.checkRect(g,new Rectangle(50, 700, 175, 50),mouse,Color.red);

        g.setFont(new Font("Futura", Font.BOLD, 50));
        g.drawString("BACK", 50,750);

        TrainingTool.checkRect(g,deleteClientRect,mouse,Color.red);

        g.setFont(new Font("Futura", Font.BOLD, 50));
        g.drawString("DELETE CLIENT", 350,750);
    }

    //Changes screens and checks for the back button being clicked.
    public void changeClientScreen(ArrayList<Exercise> exercises,int x, int y){
        if(screen == MAINSCREEN){
            if(newWorkout.contains(x,y)){
                screen = WEEKDAY;
            }
        }else if(screen == WORKOUT){
            if(new Rectangle(300, 700, 350, 50).contains(x,y)){    //Checks if the save workout button is clicked.
                largeCheckmark.setCounter(47);
            }else if(new Rectangle(800, 700, 500, 50).contains(x,y)){    //Checks if the saved workouts button is clicked.
                screen = SAVEDWORKOUTS;
            }else if(loadDaysWorkout.contains(x,y)){    //Checks if the button of loading the previous workout for the day is clicked.
                loadExercisesIntoHexagon();
                largeCheckmark.setCounter(47);
            }
        }
        if(new Rectangle(50, 700, 175, 50).contains(x,y)){    //Checks the back button for each screen and sets it accordingly.
            if(screen == WEEKDAY){
                screen = MAINSCREEN;
            }
            else if(screen == WORKOUT){
                screen = MAINSCREEN;
                for(int i = 0; i<hexagons.size(); i++){
                    hexagons.get(i).setExercise(null);   //resets the hexagon if going back to the main screen.
                    hexagons.get(i).setCheck(false);
                }
                currentExercise = 0;    //Sets the index of the current exercise to the start.
            }
            else if(screen == EXERCISES){
                screen = WORKOUT;
            }
            else if(screen == EXERCISEDESC){
                screen = EXERCISES;
            }else if(screen == SETS) {
                screen = WORKOUT;
                ChangeRepsandSets();
            }else if(screen == SAVEDWORKOUTS){
                screen = WORKOUT;
            }else if(screen == SAVEDWORKOUT){
                screen = SAVEDWORKOUTS;
            }
        }
        changeClientScreen2(exercises,x,y);
    }

    //Sets the current workout's current exercise to the sets and reps that were inputted in the text box.
    public void ChangeRepsandSets(){

        //Changes the current exercise to the sets and reps that was inputted.
        if(setsReps.getReps() == "" || setsReps.getSets() =="") {
            week[currentWorkout].getWorkout().remove(currentExercise);
        }else{
            if(hexagons.get(currentHexagon).getCheck()){    //If the hexagon had already been changed then it makes sure the new current exercise is correct when changing the values of that exercise.

                week[currentWorkout].getWorkout().get(currentExercise).setReps(setsReps.getReps());
                week[currentWorkout].getWorkout().get(currentExercise).setSets(setsReps.getSets());
                week[currentWorkout].getWorkout().get(currentExercise).setExtraInfo(setsReps.getExtra());

                //adds the exercise to the current hexagon to be drawn.
                setsReps.setSets("");
                setsReps.setReps("");
                setsReps.setExtra("");
                hexagons.get(currentHexagon).setExercise(week[currentWorkout].getWorkout().get(currentExercise));
                hexagons.get(currentHexagon).setCheck(true);

                currentExercise = prevExercise;
                prevExercise = 0;
            } else {
                week[currentWorkout].getWorkout().get(currentExercise).setReps(setsReps.getReps());
                week[currentWorkout].getWorkout().get(currentExercise).setSets(setsReps.getSets());
                week[currentWorkout].getWorkout().get(currentExercise).setExtraInfo(setsReps.getExtra());

                //adds the exercise to the current hexagon to be drawn and resets the values.
                setsReps.setSets("");
                setsReps.setReps("");
                setsReps.setExtra("");
                hexagons.get(currentHexagon).setExercise(week[currentWorkout].getWorkout().get(currentExercise));
                hexagons.get(currentHexagon).setCheck(true);

                currentExercise += 1;
            }
        }

    }

    //changes screens for the hexagons, checkmarks, days of the week in the WEEKDAY screen, and the exercises.
    public void changeClientScreen2(ArrayList<Exercise> exercises,int x, int y){

        String [] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        if(screen == WEEKDAY) {
            for (int i = 0; i < dayRects.length; i++) {
                if (dayRects[i].contains(x, y)) {
                    screen = WORKOUT;
                    currentWorkout = i;
                    Workout tmpWorkout = week[currentWorkout];
                    loadWorkoutIn = tmpWorkout;   //Saves the previous workout to be used.
                    week[currentWorkout] = new Workout(days[i]);
                }
            }
        }

        else if(screen == WORKOUT) {
            for (int i = 0; i < hexagons.size(); i++) {    //sets the current exercise to the hexagon that was pressed and previous if the hexagon was already changed.
                if (hexagons.get(i).getHexagon().contains(x, y)) {
                    screen = EXERCISES;
                    currentHexagon = i;
                    if(hexagons.get(i).getCheck()){
                        prevExercise = currentExercise;
                        currentExercise = i;
                    }
                }
            }
        }

        else if(screen == EXERCISES) {
            for (int i = 0; i < checks.size(); i++) {    //loops through the check marks.
                if (checks.get(i).getCheckmark().contains(x, y)) {     //if the checkmark contains the x and y position of the mouse it takes the ID of the check mark which corresponds to an exercise.
                    int ID = checks.get(i).getID();
                    for (int e = 0; e < exercises.size(); e++) {
                        if (ID == exercises.get(e).getID()) {    //Finds the ID it corresponds to in the exercise library.
                            if(hexagons.get(currentHexagon).getCheck()){
                                week[currentWorkout].getWorkout().set(currentExercise,new Exercise(exercises.get(e)));     //Sets the workout to that exercise.
                                screen = SETS;
                            }else{
                                week[currentWorkout].addExercise(new Exercise(exercises.get(e)));
                                screen = SETS;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            if(Exercise.checkExerciseRect(exercises,x,y)){
                screen = EXERCISEDESC;
                ExerciseDescription = Exercise.getExercise(exercises,x,y);
            }
        }
        else if(screen == EXERCISEDESC){     //Checks the rectangle to play the video in the exercise description.
            Point p = new Point(x,y);
            ExerciseDescription.playVideo(p);
        }
    }

    //adds checkmarks to the arraylist
    public void loadChecks(ArrayList<Exercise> exercises, Graphics g) {

        g.setFont(new Font("Futura", Font.PLAIN, 25));
        int counter = 0;

        for (int x = 100; x <= 1400; x += 260) {
            for (int y = 200; y <= 700; y += 40) {
                if (counter >= exercises.size()) {
                    break;
                }

                int width = g.getFontMetrics().stringWidth(exercises.get(counter).getName());
                checks.add(new CheckMark(x+width+10,y, exercises.get(counter).getID()));
                counter += 1;
            }
        }
    }
}

//Loads all the info in from the text files and holds it.
class TextFileInfo {

    private ArrayList<String> ExerciseLibrary;    //Exercise library with all the current exercises
    private ArrayList<String> allPeople;    //all the clients and workouts.
    private ArrayList<String> savedWorkouts;    //all the saved workouts.

    //to print the number of records in the create exercise screen so i know what record we are at.
    private int recordnum;

    public TextFileInfo() {
        ExerciseLibrary = new ArrayList<>();
        savedWorkouts = new ArrayList<>();
        allPeople = new ArrayList<>();
        recordnum = 0;

        try {
            loadInfo();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public ArrayList<String> getExerciseLibrary() {
        return ExerciseLibrary;
    }

    public void setExerciseLibrary(ArrayList<String> exerciseLibrary) {
        ExerciseLibrary = exerciseLibrary;
    }

    public int getRecordnum(){
        return recordnum;
    }

    public void setRecordnum(){
        recordnum++;
    }

    //Loads info in by looping through the lines in the file until there is none left.
    public void loadInfo() throws FileNotFoundException {

        Scanner inFile = new Scanner(new BufferedReader(new FileReader("training.txt")));

        while (inFile.hasNext()) {
            ExerciseLibrary.add(inFile.nextLine());
            recordnum += 1;
        }
        inFile.close();

        Scanner inFile2 = new Scanner(new BufferedReader(new FileReader("allTraining.txt")));
        while (inFile2.hasNext()) {
            allPeople.add(inFile2.nextLine());
        }
        inFile2.close();

        Scanner inFile3 = new Scanner(new BufferedReader(new FileReader("savedWorkouts.txt")));

        while (inFile3.hasNext()) {
            savedWorkouts.add(inFile3.nextLine());
        }
        inFile3.close();

    }

    //Returns an Arraylist of workout object arrays that contain each week of training for each client.
    public ArrayList<Workout[]> getWorkouts2(ArrayList<Exercise> exercises){
        ArrayList<Workout[]> clientWeeks = new ArrayList<Workout[]>();
        Workout[] workoutWeek = new Workout[7];
        String [] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        //Temporary array to get the name of the client.
        ArrayList<String> tmpPersonWorkouts = new ArrayList<>();

        //loops through all the info in the text file that was loaded in. Checks to see if it doesn't contain a semi colon which signals a new
        //client's name has appeared. It then adds the workouts to the week.
        for(int i = 1; i< allPeople.size(); i++){
            if(!allPeople.get(i).contains(";") && !allPeople.get(i).equals("")){
                //if condition is met it cycles through the arraylist of workouts in the client. And adds them to the week of workouts which is then
                //added to the arraylist of workout weeks.
                for(int e = 0; e<tmpPersonWorkouts.size(); e++){
                    //Constructs a workout from the Arraylist of strings containing the exercises.

                    Workout tmp = new Workout(tmpPersonWorkouts.get(e),exercises);
                    for(int w = 0; w<workoutWeek.length; w++){
                        //Looks through the week to check if the day of the workout is equal to the index of the week array, then
                        //adds the workout to that specific day in the week.
                        if(tmp.getDay().equals(days[w])){
                            workoutWeek[w] = tmp;
                            break;
                        }
                    }
                }
                //Adds the week and resets the temporary arrays.
                clientWeeks.add(workoutWeek);
                workoutWeek = new Workout[7];
                tmpPersonWorkouts = new ArrayList<>();
            }
            tmpPersonWorkouts.add(allPeople.get(i));
        }

        //Does the same thing above but since it checks until a new person comes since a new person won't come on the last time in the text file i need
        //to execute the same code once more.
        for(int e = 0; e<tmpPersonWorkouts.size(); e++){
            Workout tmp = new Workout(tmpPersonWorkouts.get(e),exercises);
            for(int w = 0; w<workoutWeek.length; w++){
                if(tmp.getDay().equals(days[w])){
                    workoutWeek[w] = tmp;
                    break;
                }
            }
        }
        clientWeeks.add(workoutWeek);
        return clientWeeks;
    }

    //Gets the workouts from the saved workouts file and returns them as an Arraylist of workouts.
    public ArrayList<Workout> getWorkouts(ArrayList<Exercise> exercises){
        ArrayList<Workout> saved = new ArrayList<>();
        ArrayList<String> tmpArraylist = savedWorkouts;

        for(int i = 0; i<tmpArraylist.size(); i++){
             saved.add(new Workout(tmpArraylist.get(i),exercises));
        }
        return saved;
    }

    //splits info for the exercise library into exercises.
    public ArrayList<Exercise> splitInfo(){
        ArrayList<Exercise> exercises = new ArrayList<>();
        for(int i = 0; i< ExerciseLibrary.size(); i++){
            try{
                exercises.add(new Exercise(ExerciseLibrary.get(i)));
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        return exercises;
    }

    //Splits the info for the clients and creates client objects using the name of the people by checking for semi-colons in the string since all the
    //other info in the file has them.
    public ArrayList<Client> splitInfo2(){
        ArrayList<Client> clients = new ArrayList<Client>();
        for(int i = 0; i< allPeople.size(); i++){
            if(!allPeople.get(i).contains(";") && allPeople.get(i) != ""){
                clients.add(new Client(allPeople.get(i)));
            }
        }
        return clients;
    }
}

//class for things corresponding to the hexagon in the workout screen of the client.
class Hexagon {
    //A polygon that is used to check positions
    private Polygon hexagon;
    //x,y position of the hexagon
    private int x;
    private int y;
    //size
    private int size;
    //A boolean to make sure that once it is clicked and an exercise is inputted it draws something else.
    private boolean check;
    //The exercise needed to draw in the hexagon.
    private Exercise exercise;

    public Hexagon(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        hexagon = createHexagon();
        check = false;
    }

    public void setCheck(boolean check){
        this.check = check;
    }

    public boolean getCheck(){
        return check;
    }
    
    public Polygon getHexagon(){
        return hexagon;
    }

    public void setExercise(Exercise ex){
        exercise = ex;
    }

    //used to create hexagon
    public static Point xy(double ang, double mag, int x, int y) {
        int px = (int) (x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int) (y - Math.sin(Math.toRadians(ang)) * mag);
        return new Point(px, py);
    }

    //draws according to the check variable
    public void drawHexagon(Graphics g, Point mouse){
        if(!check) {
            g.setColor(Color.black);
            try {
                if (hexagon.contains(mouse)) {
                    g.setColor(Color.red);

                }

            } catch (Exception e) {
            }
            g.fillPolygon(hexagon);
        }
        if(check){
            g.setColor(Color.black);
            try {
                if (hexagon.contains(mouse)) {
                    g.setColor(Color.red);

                }

            } catch (Exception e) {
            }
            g.drawPolygon(hexagon);
            drawInHexagon(g);

        }
    }


    //Draws all the info in the hexagon like the sets and reps. Uses the width of each element in the exercise to attempt to centre it.
    public void drawInHexagon(Graphics g){
        g.setFont(new Font("Futura", Font.BOLD, 18));
        g.drawString(exercise.getSets() + "x" + exercise.getReps(), x-21,y-150);
        String exercise2;
        String exercise3;
        int size = g.getFontMetrics().stringWidth(exercise.getName());

        if(size > 120){
            exercise2 = exercise.getName().substring(0,exercise.getName().length()/2);
            exercise3 = exercise.getName().substring(exercise.getName().length()/2);
            g.drawString(exercise2, x - 42, y - 130);
            g.drawString(exercise3, x - 42, y - 110);
        }else if(size <50){
            g.drawString(exercise.getName(), x - 28, y - 130);
        }
        else if(size<60){
            g.drawString(exercise.getName(), x - 34, y - 130);
        }else if(size<80){
            g.drawString(exercise.getName(), x - 43, y - 130);
        }else if(size <100){
            g.drawString(exercise.getName(), x - 50, y - 130);
        }
        else{
            g.drawString(exercise.getName(), x - 58, y - 130);
        }
    }

    //creates the hexagon
    private Polygon createHexagon() {
        int[] xPoints = new int[7];
        int[] yPoints = new int[7];
        Point start = xy(90, 2 * size, x, y - size);
        xPoints[0] = start.x;
        yPoints[0] = start.y;
        Point forNext = start;
        for (int i = 0; i < 6; i++) {    //just rotates and moves the distance to create the regular hexagon.
            start = xy(-30 + i * -60, size, forNext.x, forNext.y);
            xPoints[i] = start.x;
            yPoints[i] = start.y;
            forNext = start;
        }

        Polygon hex = new Polygon(xPoints, yPoints, 6);

        return hex;
    }

}

//Class for the checkmarks on the screen in the workout screen of the client class. Used to select exercises.
class CheckMark {
    //The polygon for the checkmark
    private Polygon checkMark;
    //the x,y position
    private int x;
    private int y;
    private int size;    //size of the checkmark.
    private boolean drawCheck;    //checks to draw the large checkmark.
    private int linkedID;    //Linked ID to see which exercise corresponds to the checkmark.
    private int counter;    //counter to draw the check mark for a certain period of time.

    public CheckMark(int x, int y, int ID) {
        this.x = x;
        this.y = y;
        linkedID = ID;
        checkMark = createCheck();
    }

    //Constructs the large checkmark.
    public CheckMark(int x, int y, int size, String nun) {
        this.x = x;
        this.y = y;
        this.size = size;
        checkMark = createCheck(size);
        drawCheck = false;
        counter = 0;
    }

    public void setCounter(int c){
        counter = c;
    }

    public int getID(){
        return linkedID;
    }

    public Polygon getCheckmark(){
        return checkMark;
    }

    public void countDown(){
        if(counter > 0){
            counter -= 1;
            drawCheck = true;
        }else{
            counter = 0;
            drawCheck = false;
        }
    }

    //used to draw.
    public static Point xy(double ang, double mag, int x, int y) {
        int px = (int) (x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int) (y - Math.sin(Math.toRadians(ang)) * mag);
        return new Point(px, py);
    }

    // draws the check mark.
    public void drawCheckmark(Graphics g, Point mouse){
        g.setColor(Color.black);

        try {
            if (checkMark.contains(mouse)) {
                g.setColor(Color.green);

            }
        } catch (Exception e) {
        }

        g.fillPolygon(checkMark);
    }

    public void drawCheckmark(Graphics g){
        if(drawCheck) {
            g.setColor(Color.green);
            g.fillPolygon(checkMark);
        }else if(!drawCheck){

        }
    }

    //creates the check mark.
    private Polygon createCheck(){
        int[] xPoints = new int[7];
        int[] yPoints = new int[7];
        xPoints[0] = x;
        yPoints[0] = y;

        Point next = xy(30, 30, x, y);
        xPoints[1] = next.x;
        yPoints[1] = next.y;

        Point forNext = xy(120,7, next.x, next.y);
        next = forNext;

        xPoints[2] = next.x;
        yPoints[2] = next.y;

        forNext = xy(210,20, next.x, next.y);
        next = forNext;

        xPoints[3] = next.x;
        yPoints[3] = next.y;

        forNext = xy(120,7, next.x, next.y);
        next = forNext;

        xPoints[4] = next.x;
        yPoints[4] = next.y;

        forNext = xy(210,7, next.x, next.y);
        next = forNext;

        xPoints[5] = next.x;
        yPoints[5] = next.y;

        xPoints[6] = x;
        yPoints[6] = y;

        Polygon check = new Polygon(xPoints, yPoints, 6);

        return check;
    }

    //creates the larger check mark by going according to a size.
    private Polygon createCheck(int size){
        int[] xPoints = new int[7];
        int[] yPoints = new int[7];
        xPoints[0] = x;
        yPoints[0] = y;

        Point next = xy(30, 30 *size, x, y);
        xPoints[1] = next.x;
        yPoints[1] = next.y;

        Point forNext = xy(120,7*size, next.x, next.y);
        next = forNext;

        xPoints[2] = next.x;
        yPoints[2] = next.y;

        forNext = xy(210,20*size, next.x, next.y);
        next = forNext;

        xPoints[3] = next.x;
        yPoints[3] = next.y;

        forNext = xy(120,7*size, next.x, next.y);
        next = forNext;

        xPoints[4] = next.x;
        yPoints[4] = next.y;

        forNext = xy(210,7*size, next.x, next.y);
        next = forNext;

        xPoints[5] = next.x;
        yPoints[5] = next.y;

        xPoints[6] = x;
        yPoints[6] = y;

        Polygon check = new Polygon(xPoints, yPoints, 6);

        return check;
    }


}

//This class makes objects that can be used to draw a screen that takes in inputs and creates a string.
//This class is used for any space where I made a screen for a textbox.
class TypingBox{

    //Texts split into 2 just incase the length of the string is too long.
    private String text;
    private String text2;

    private int stringWidth;    //Width of the string(usually for the description of the exercise.)
    //The keycode of the key that was pressed
    private int code;
    //All the entries that have happened
    private ArrayList<String> entries;

    //Sets reps and extra inputted into the exercsie object.
    private String sets;
    private String reps;
    private String extra;
    private String clientName;    //Client name for the screen in which I create a new client.
    //counter used to input sets and reps
    private int counter;

    public TypingBox(){
        text2="";
        text="";
        entries = new ArrayList<String>();
        stringWidth = 0;
        code=0;
        sets = "";
        reps ="";
        extra = "";
        clientName = "";
        counter = 0;
    }

    public void setEntries(ArrayList<String> entries) {
        this.entries = entries;
    }

    public ArrayList<String> getEntries(){
        return entries;
    }

    public String getReps() {
        return reps;
    }

    public String getExtra() {
        return extra;
    }

    public String getSets() {
        return sets;
    }

    public void setSets(String sets) {
        this.sets = sets;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public void setExtra(String e){
        extra = e;
    }

    public String getClientName(){
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    //gets the key that was pressed.
    public void getKeyCode(KeyEvent e){
        code = e.getKeyCode();
    }

    //This checks for the string length makes sure it doesn't get off the screen and adds and subtracts the keys inputted on and off of a string.
    public TextFileInfo keyTyping(KeyEvent e){
        TextFileInfo info = TrainingTool.getInformation();
        if(code == KeyEvent.VK_ENTER){
            String text3 = text + text2;
            if(text3.split(";").length == 4){    //Makes sure that they use the right way of inputting the information to cause no issues while adding exercises to the text file.
                text = text + text2;
                entries.add(text);
                info.setRecordnum();    //Makes sure to set the number of records for the exercise library.
                text = "";
                text2 = "";
            }else{
                System.out.println("Error");
            }

        }
        //Subtracts to the corresponding text according to the size of the string.
        else if(code == KeyEvent.VK_BACK_SPACE && text.length()>0){
            if(stringWidth>1000 && !Objects.equals(text2, "")){
                text2 = text2.substring(0,text2.length()-1);
            }else{
                text = text.substring(0,text.length()-1);
            }

        }
        else{
            if(stringWidth>1000){
                text2 += e.getKeyChar();
            }else{
                text += e.getKeyChar();
            }
        }

        return info;
    }

    //A different version of key typing for the sets and reps when making a workout.
    public void keyTyping2(KeyEvent e){

        if(code == KeyEvent.VK_ENTER){
            if(counter == 0){    //checks how many times enter is clicked and changes the variable being changed to text each time.
                sets = text;
                counter ++;
            }else if(counter == 1){
                reps = text;
                counter ++;
            }else{
                extra = text;
                counter = 0;
            }

            text = "";
        }

        else if(code == KeyEvent.VK_BACK_SPACE && text.length()>0){    //same back space.
            if(stringWidth>1000 && !Objects.equals(text2, "")){
                text2 = text2.substring(0,text2.length()-1);
            }else{
                text = text.substring(0,text.length()-1);
            }
        }
        else{
            if(stringWidth>1000){
                text2 += e.getKeyChar();
            }else{
                text += e.getKeyChar();
            }
        }
    }

    //A different version of key typing for changing the clients name.
    public void keyTyping3(KeyEvent e){
        if(code == KeyEvent.VK_ENTER){
            clientName = text;    //changes the clients name to what is inputted.
            text = "";
        }

        else if(code == KeyEvent.VK_BACK_SPACE && text.length()>0){
            text = text.substring(0,text.length()-1);
        }
        else{
            text += e.getKeyChar();
        }
    }


    //draws for the exercises on the screen for the textbox in the main class.
    public void newExerciseDraw(Graphics g, Point mouse, int width, int height){
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width, height);
        textBox(g);
        Font font = new Font ("Arial", Font.BOLD, 35);
        Font font2 = new Font ("Arial", Font.BOLD, 80);

        g.setFont(font2);
        g.drawString("Create an exercise", 350,80);
        g.setFont(font);
        g.drawString("Must go back to intro to apply.", 850, 730);
        g.drawString(TrainingTool.getInformation().getRecordnum() + "", 30, 60);
        g.setFont(new Font("Futura", Font.BOLD, 18));
        g.setColor(Color.green);
        g.drawString("SAMPLE: ID;name;description;youtube video link", 30,120);

        g.setFont (new Font ("Arial", Font.BOLD, 16));
        g.setColor(Color.BLUE);

        int y=0;
        //loops through all the entries and draws then according to the x and y inputted and draws them down on the side of the screen.
        if(entries!= null){
            for(String s : entries){
                if(g.getFontMetrics().stringWidth(s) > 900){    //breaks the string in half if it passes a certain size and draws it.
                    String start = s.substring(0,s.length()/2);
                    String end = s.substring(s.length()/2);
                    g.drawString(""+start, 480, y* 25 + 300);
                    g.drawString(""+end, 480, y +1 * 25 + 300);
                    y+= 3;
                }else{
                    g.drawString(""+s, 480, y* 25 + 300);
                    y++;
                }
            }
        }
        TrainingTool.checkRect(g,new Rectangle(50,700,175,50),mouse,Color.red);

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("BACK", 50,750);
    }

    //draws for the sets and reps
    public void setsRepsDraw(Graphics g, Point mouse, int width, int height){
        g.setColor(new Color(96, 189, 96));
        g.fillRect(0,0,width,height);
        textBox(g);
        Font font = new Font ("Arial", Font.BOLD, 35);
        Font font2 = new Font ("Arial", Font.BOLD, 80);

        g.setFont(font2);
        if(counter == 0){    //checks the counter and asks the question so that the proper variables can be changed.
            g.drawString("List the number of sets:", 300,80);
        }else if(counter == 1){
            g.drawString("List the number of reps:", 300,80);
        }else if(counter == 2){
            g.drawString("List any other information:", 300,80);
        }

        g.setFont(font);
        g.drawString("Must go back to intro to apply.", 700, 730);

        g.setFont (new Font ("Arial", Font.BOLD, 40));
        g.setColor(Color.RED);

        g.drawString(sets + "X" + reps, 550,  200);
        g.drawString(extra, 550,  250);


        g.setColor(Color.black);
        TrainingTool.checkRect(g,new Rectangle(50,700,175,50),mouse,Color.red);

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("BACK", 50,750);
    }

    //draws all the graphics for the new client that's added into the screen. Same concept as all the other methods that draw graphics for text boxes only difference
    //is that it uses the client name variable.
    public void newClientdraw(Graphics g, Point mouse, int width, int height, Image curls){
        g.setColor(Color.white);
        g.fillRect(0,0,width,height);
        textBox(g);
        Font font2 = new Font ("Futura", Font.BOLD, 80);

        g.drawImage(curls,700,370, null);
        g.setFont(font2);
        g.drawString("Type in the Clients Name!", 200,120);

        g.setFont(new Font("Arial", Font.BOLD, 50));
        TrainingTool.checkRect(g,new Rectangle(1100, 700, 275, 50),mouse,Color.blue);
        g.drawString("CONFIRM", 1100, 750);

        g.setFont (font2);
        g.setColor(Color.RED);

        g.drawString(clientName, 550,  200);

        g.setColor(Color.black);
        TrainingTool.checkRect(g,new Rectangle(50,700,175,50),mouse,Color.red);

        g.setFont(new Font("Futura", Font.BOLD, 50));
        g.drawString("BACK", 50,750);
    }

    //draws the text box
    public void textBox(Graphics g){
        g.setFont (new Font ("Arial", Font.PLAIN, 15));
        int stringWidth2 = g.getFontMetrics().stringWidth(text2);

        if(stringWidth<190){    //Checks the string width to move the box with the person who is typing and fills it white.
            g.setColor(Color.WHITE);
            g.fillRect(200,200,200,30);
            g.setColor(Color.BLACK);
            g.drawRect(200,200,200,30);
        }else if(stringWidth >1000){  //moves the boz down.
            g.setColor(Color.WHITE);
            g.fillRect(200,200,stringWidth+20,30);
            g.setColor(Color.BLACK);
            g.drawRect(200,200,stringWidth+20,30);

            g.setColor(Color.WHITE);
            g.fillRect(200,230,stringWidth2+20,30);
            g.setColor(Color.BLACK);
            g.drawRect(200,230,stringWidth2+20,30);
        }else{
            g.setColor(Color.WHITE);
            g.fillRect(200,200,stringWidth+20,30);
            g.setColor(Color.BLACK);
            g.drawRect(200,200,stringWidth+20,30);
        }


        //Draws both texts to make sure that all the things being inputted are shown.
        g.drawString(text, 205,225);
        g.drawString(text2, 205,250);
        stringWidth = g.getFontMetrics().stringWidth(text);

        if(stringWidth>1000){     //draws the box around the text.
            int offset = g.getFontMetrics().stringWidth(text2);
            g.fillRect(200+offset+5,235,3,15);
        }else{
            int offset = stringWidth;
            g.fillRect(200+offset+5,210,3,15);
        }
    }
}


//Off stack overFlow just wanted to click links to youtube videos and this worked perfectly.
class YoutubePlay
{
    public static void getLink(String Link) throws URISyntaxException {
        final URI uri = new URI(Link);
        class OpenUrlAction implements ActionListener
        {
            @Override public void actionPerformed(ActionEvent e) {
                open(uri);
            }
        }
        JFrame frame = new JFrame("Links");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(100, 400);
        Container container = frame.getContentPane();
        container.setLayout(new GridBagLayout());
        JButton button = new JButton();
        button.setText("<HTML>Click the <FONT color=\"#000099\"><U>link</U></FONT>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBackground(Color.WHITE);
        button.setToolTipText(uri.toString());
        button.addActionListener(new OpenUrlAction());
        container.add(button);
        frame.setVisible(true);
    }
    private static void open(URI uri)
    {
        if (Desktop.isDesktopSupported())
        {
            try
            {
                Desktop.getDesktop().browse(uri);
            }
            catch (IOException e)
            { /* TODO: error handling */ }
        }
        else
        { /* TODO: error handling */ }
    }
}

//The animation class sets up images and moves them by a frame and counter.
class Animation {
    private Image [] images;    //The images being changed.
    private int frame;    //The current frame.
    private int count;    //The counter for when they are changed.

    public Animation(Image [] images){
        frame = 0;
        this.images = images;
        count = 0;
    }

    //Draws the current image and increases the frames and counter.
    public void moveImages(Graphics g, int x, int y){
        g.drawImage(images[frame],x,y,null);

        count ++;
        if(count >= 2){
            frame ++;
            count = 0;
        }

        if(frame == images.length-1){
            frame = 0;
        }
    }

    //Loads all the images for the running animation.
    public static Image [] loadRunningImages(){
        Image [] images = new Image[20];
        for(int i = 0; i < images.length; i++){
            images[i] = new ImageIcon("running/running" + i+ ".png").getImage();
        }
        return images;
    }

    //Loads all the images for the jump rope animation.
    public static Image [] loadJumpRopeImages(){
        Image [] images = new Image[16];
        for(int i = 0; i < images.length; i++){
            if(i<10) {
                images[i] = new ImageIcon("tile/tile00" + i + ".png").getImage();
            }else{
                images[i] = new ImageIcon("tile/tile0" + i + ".png").getImage();
            }
        }
        return images;
    }
}