import java.util.*;
import java.io.*;

public class TSP
{
    public static void main(String[] args) 
    {
        Dictionary dic = new Dictionary();
        Coord school[] = new Coord[dic.getSize()];
        double distanceMatrix[][] = new double[121][121];
        //====================================================
        for(int i = 0; i < dic.getSize(); i++)
        {
            school[i] = new Coord();
        }
        for(int i = 0; i < dic.getSize(); i++)
        {
            String ar[] = dic.getWord(i).split(",", 3);
            school[i].setLat((Double.parseDouble(ar[1])));
            school[i].setLonk((Double.parseDouble(ar[2])));
        }
        for(int column = 0; column < 121; column++)
        {
            for(int row = 0; row < 121; row++)
            {
                distanceMatrix[row][column] = distanceChecker(school[column], school[row]);
            }
        }

        bestPath pBest = new bestPath(1000000000, "");
        
        bestPath testPath = new bestPath();
        for(int c = 0; c < 121; c++)
        {
            testPath = greedyPath(school, distanceMatrix, pBest, c);
            if(testPath.getPathLength() < pBest.getPathLength())
            {
                pBest = testPath; //remember to set starting position to firs\t part of
            }
        }
        pBest.setPath(pBest.getPath() + "0,");
        //p = greedyPath(school, distanceMatrix, p);
        System.out.println(pBest.getPath()); 
        System.out.println("Path Length is " + pBest.getPathLength() + " m");  
    }
    public static bestPath greedyPath(Coord s[], double distanceCopy[][], bestPath p, int c)
    {
        bestPath x = new bestPath();
        Random r = new Random();
        //double distance[][] = distanceCopy;// tthis only changes the refrance
        double distance[][]= new double[121][121];
        for(int column = 0; column < 121; column++)
        {
            for(int row = 0; row < 121; row++)
            {
                distance[row][column] = distanceCopy[row][column];
            }
        }

        for(int col = 0; col < 121; col++)
        {
            distance[0][col] = 1000001.0;
        }

        for(int column = 0; column < 121; column++)
        {
            for(int row = 0; row < 121; row++)
            {
                distance[row][column] += (double)r.nextInt(20);
            }
        }
        //int startingPos = r.nextInt(119) + 1;
        int col = c;
        int nextCol = 0;
        int lastRow = 0;
        double shortest = 0.0;
        for(int i = 0; i < 120; i++)
        {
            shortest = 1000000.0;
            for(int row = 0; row < 121; row++)
            {
                if (distance[row][col] < shortest)
                {
                    shortest = distance[row][col];
                    nextCol = row;
                }
            }
            lastRow = col;
            col = nextCol;
            x.setPath(x.getPath()+ Integer.toString(nextCol) + ",");
            x.setPathLength(x.getPathLength() + shortest);

            for(int column = 0; column < 121; column++)// seeting everything in that row to large number so won't be visited again
            {
                distance[lastRow][column] = 1000001.0;
            }
        }
        return x;
    }
    public static double distanceChecker(Coord x, Coord y)
    {
        final double RADIUS = 6371000; //in meteres

        double lat1 = x.getLat();
        double lat2 = y.getLat();
        double lon1 = x.getLonk();
        double lon2 = y.getLonk();
        //The following is in rads
        double lat1Rad = (lat1 * Math.PI)/180;
        double lat2Rad = (lat2 * Math.PI)/180;
        double latDif = ((lat2 - lat1) * Math.PI)/180;
        double lonDif = ((lon1 - lon2) * Math.PI)/180;

        double a  = Math.sin(latDif/2) * Math.sin(latDif/2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(lonDif/2) * Math.sin(lonDif/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = RADIUS * c;
        //Distance, in meters
        if(d == 0.0)
        {
            return 1000000.0;
        }
        return d;
    }
}
class bestPath
{
    private double pathLength;
    private String path;

    public bestPath()
    {
        pathLength = 0;
        path = "0,";
    }
    public bestPath(double x, String y)
    {
        pathLength = x;
        path = y;
    }
    public double getPathLength(){ return pathLength; }
    public String getPath(){ return path; }

    public void setPathLength(double x){ pathLength = x;}
    public void setPath(String x){path = x;}
    
}
class Coord
{
    private double lat;
    private double lonk;

    public Coord()
    {
        lat = 0.0;
        lonk = 0.0;
    }
    public Coord(double lat, double lonk)
    {
        this.lat = lat;
        this.lonk = lonk;
    }

    public double getLat(){return lat;}
    public double getLonk(){return lonk;}

    public void setLat(double a){lat = a;}
    public void setLonk(double a){lonk = a;}
}  
class Dictionary
{
    //Dictionary class for reading in txt.
     
    private String input[]; 

    public Dictionary(){
        input = load("C:\\Users\\user\\Desktop\\120schools.txt");  
    }
    
    public int getSize(){
        return input.length;
    }
    
    public String getWord(int n){
        return input[n].trim();
    }
    
    private String[] load(String file) {
        File aFile = new File(file);     
        StringBuffer contents = new StringBuffer();
        BufferedReader input = null;
        try {
            input = new BufferedReader( new FileReader(aFile) );
            String line = null; 
            int i = 0;
            while (( line = input.readLine()) != null){
                contents.append(line);
                i++;
                contents.append(System.getProperty("line.separator"));
            }
        }catch (FileNotFoundException ex){
            System.out.println("Can't find the file - are you sure the file is in this location: "+file);
            ex.printStackTrace();
        }catch (IOException ex){
            System.out.println("Input output exception while processing file");
            ex.printStackTrace();
        }finally{
            try {
                if (input!= null) {
                    input.close();
                }
            }catch (IOException ex){
                System.out.println("Input output exception while processing file");
                ex.printStackTrace();
            }
        }
        String[] array = contents.toString().split("\n");
        for(String s: array){
            s.trim();
        }
        return array;
    }
}
