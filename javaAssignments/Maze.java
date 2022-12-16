import java.util.*;

public class Maze{
    
    public static void main(String[] args){
        int lives = 500;
        String[] input = new String[20];
        input[ 0]="XXX XXXXXXX XXXXXX X";
        input[ 1]="XXX XXXXXXX XXXXXX X";
        input[ 2]="XXX      XX XXXX   X";
        input[ 3]="XXXXXXX XXXXXXXXXXXX";
        input[ 4]="XXXXXXX XX      XXXX";
        input[ 5]="XXX  XXXXX XXXX XXXX";
        input[ 6]="XX  X XXXX   XX XXXX";
        input[ 7]="XXX XXXXXXXX XX XXXX";
        input[ 8]="XX  X  XXXXX XX XXXX";
        input[ 9]="XXXXXX       XX XXXX";
        input[10]="X XXXX XX  XXXX XXXX";
        input[11]="     XXXX  XXXX XXXX";
        input[12]="XXXXXXXXXXXXXXX XXXX";
        input[13]="XXXXXX  XXXX    XXXX";
        input[14]="XX XX XXXXXX XX XXXX";
        input[15]="X  XX XXXXXX XX XXXX";
        input[16]="XX XX X  X   XX XX  ";
        input[17]="X  XXXXXXX XXXX XX X";
        input[18]="XX XXXXXXX XXXXXXX X";
        input[19]="XX XXXXXXX XXXXXXX X";
        int posX=10;
        int posY=10;
        
        boolean[][] maze = new boolean[20][20];
        for(int i=0;i<20;i++){
            for(int j=0;j<20;j++){
                if(input[j].charAt(i)=='X'){
                    maze[i][j]=false;
                }else{
                    maze[i][j]=true;
                }
            }
        }
        System.out.println(posX+" "+posY);
        printboard(maze,posX,posY);
        Brain myBrain = new Brain(); //boolean maze passed into brain class
        
        
        while(lives>0){
            String move =myBrain.getMove(maze[posX][posY-1],maze[posX][posY+1],maze[posX+1][posY],maze[posX-1][posY]);
            if(move=="north"&&maze[posX][posY-1]){
                posY--;
            }else if(move=="south"&&maze[posX][posY+1]){
                posY++;
            }else if(move=="east"&&maze[posX+1][posY]){
                posX++;
            }else if(move=="west"&&maze[posX-1][posY]){
                posX--;
            }
            System.out.println(posX+" "+posY+" "+lives);
            printboard(maze,posX,posY);
            lives--;
            if(posY%19==0||posX%19==0){
                System.out.println(posX+","+posY);
                System.exit(0);
            }
        }
        System.out.println("You died in the maze!");
    }

    
    public static void printboard(boolean[][] board, int posX, int posY){
        for(int y=0;y<20;y++){
            for(int x=0;x<20;x++){
                if(x==posX&&y==posY){
                    System.out.print(":)");
                }else{
                    if(board[x][y]==true){
                        System.out.print("  ");
                    }else{
                        System.out.print("■ ");
                    }
                }
            }
            System.out.println();
        }
        try{
            Thread.sleep(100);
        }catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}

class Brain{
    
    private int visited[][] = new int[41][41];// 0 unvisited, 1 visited, 2 stick
    private int posx = 20;
    private int posy = 20;
    private  Stack<String> lastMove = new Stack<String>();

    public String getMove(boolean north, boolean south, boolean east, boolean west)
    {
        //=====================================================================
        //this section gives available directions
        int numOfDirections = 0;
        int position = 0;
        String availableDirections[];

        if((north == true) && (visited[posx][posy-1] == 0))
        {
            numOfDirections++;
        }
        if((south == true) && (visited[posx][posy+1] == 0))
        {
            numOfDirections++;
        }
        if((east == true) && (visited[posx+1][posy]  == 0))
        {
            numOfDirections++;
        }
        if((west == true) && (visited[posx-1][posy]  == 0))
        {
            numOfDirections++;
        }
        System.out.println(numOfDirections);
        //==========================================================
        availableDirections = new String[numOfDirections];
        if(north == true && (visited[posx][posy-1] < 1))
        {
            availableDirections[position] = "north";
            position++;
        }
        if(south == true && (visited[posx][posy+1] < 1))
        {
            availableDirections[position] = "south";
            position++;
        }
        if(east == true && (visited[posx+1][posy] < 1))
        {
            availableDirections[position] = "east";
            position++;
        }
        if(west == true && (visited[posx-1][posy] < 1))
        {
            availableDirections[position] = "west";
            position++;
        }

        //================================================================
        if(numOfDirections > 1)//at junction
        {
            int random = (int)(Math.random()*numOfDirections);
            lastMove.push(availableDirections[random]);
            visited[posx][posy] = 1;
            if(availableDirections[random].equals("north")) posy--;
            if(availableDirections[random].equals("south")) posy++;
            if(availableDirections[random].equals("east")) posx++;
            if(availableDirections[random].equals("west")) posx--;
            return availableDirections[random];
        }
        else if(numOfDirections == 0)//time to backtrack
        {
            String temp = lastMove.peek();
            lastMove.pop();
            if(temp.equals("north"))
            {
                visited[posx][posy] = 2;
                posy++;
                return "south";
            }
            if(temp.equals("south"))
            {
                visited[posx][posy] = 2;
                posy--;
                return "north";
            }
            if(temp.equals("east"))
            {
                visited[posx][posy] = 2;
                posx--;
                return "west";
            }
            if(temp.equals("west"))
            {
                visited[posx][posy] = 2;
                posx++;
                return "east";
            }
        }
        else//rule 1, can only go one direction
        {
            if((north == true) && (visited[posx][posy-1] == 0))
            {
                visited[posx][posy] = 1;
                posy--;
                lastMove.push("north");
                return "north";
            }
            else if((south == true) && (visited[posx][posy+1] == 0))
            {
                visited[posx][posy] = 1;
                posy++;
                lastMove.push("south");
                return "south";
            }
            else if((east == true) && (visited[posx+1][posy]  == 0))
            {
                visited[posx][posy] = 1;
                posx++;
                lastMove.push("east");
                return "east";
            }
            else if((west == true) && (visited[posx-1][posy]  == 0))
            {
                visited[posx][posy] = 1;
                posx--;
                lastMove.push("west");
                return "west";
            }
        }
        return "";
    }
}
