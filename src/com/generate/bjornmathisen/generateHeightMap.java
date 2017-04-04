package com.generate.bjornmathisen;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.concurrent.ThreadLocalRandom;

public class generateHeightMap {

    // https://stackoverflow.com/questions/43179809/diamond-square-improper-implementation

    private static final Random RAND = new Random();
    // Size of map to generate, must be a value of (2^n+1), ie. 33, 65, 129
    // 257,1025 are fun values
    private static final int MAP_SIZE = 1025;
    // initial seed for corners of map
    private static final double SEED = ThreadLocalRandom.current().nextInt(0, 1 + 1);
    // average offset of data between points
    private static double avgOffSetInit = 1;

    private static final String PATH = "C:\\Users\\bcm27\\Desktop\\grayScale_export";
    private static String fileName = "\\grayscale_map00.PNG";

    public generateHeightMap(int howManyMaps) {
        System.out.printf("Seed: %s\nMap Size: %s\nAverage Offset: %s\n", 
                SEED, MAP_SIZE, avgOffSetInit);
        System.out.println("-------------------------------------------");

        for(int i = 1; i <= howManyMaps; i++){ // how many maps to generate

            double[][] map = populateMap(new double[MAP_SIZE][MAP_SIZE]); 
            //printMap(map);         
            generateHeightMap.greyWriteImage(map);
            fileName = "\\grayscale_map0" + i + ".PNG";

            System.out.println("Output: " + PATH + fileName);
        }
    }

    public static int[] convert2Dinto1DArray(int[][] array2Convert) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < array2Convert.length; i++) {
            for (int j = 0; j < array2Convert[i].length; j++) { 
                // store all the values in a list
                list.add(array2Convert[i][j]); 
            }
        }
        // now add these values to the list
        int[] newArray = new int[list.size()];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = list.get(i);
        }

        return newArray;
    }


    /*************************************************************************************
     * @param requires a 2d map array of 0-1 values, and a valid file path
     * @post creates a image file saved to path + file_name
     ************************************************************************************/
    private static void greyWriteImage(double[][] data) {
        BufferedImage image = 
                new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < data[0].length; y++)
        {
            for (int x = 0; x < data.length; x++)
            {// for each element in the data

                if (data[x][y]>1){
                    // tells the image whether its white
                    data[x][y]=1;
                }
                if (data[x][y]<0){
                    // tells the image whether its black
                    data[x][y]=0;
                }
                Color col = // RBG colors
                        new Color((float)data[x][y],
                                (float)data[x][y],
                                (float)data[x][y]); 
                // sets the image pixel color equal to the RGB value
                image.setRGB(x, y, col.getRGB());
            }
        }

        try {
            // retrieve image
            File outputfile = new File( PATH + fileName);
            outputfile.createNewFile();
            ImageIO.write(image, "png", outputfile);

        } catch (IOException e) {
            throw new RuntimeException("I didn't handle this very well. ERROR:\n" + e);
        }
    }

    /****************************************************************************
     * @param requires map double[MAPSIZE][MAPSIZE]
     * @return returns populated map
     * 
     * [1] Taking a square of four points, generate a random value at the square 
     *     midpoint, where the two diagonals meet. The midpoint value is calcul-
     *     ated by averaging the four corner values, plus a random amount. This 
     *     gives you diamonds when you have multiple squares arranged in a grid.
     *
     * [2] Taking each diamond of four points, generate a random value at the 
     *     center of the diamond. Calculate the midpoint value by averaging the 
     *     corner values, plus a random amount generated in the same range as 
     *     used for the diamond step. This gives you squares again.
     *     
     *     '*' equals a new value
     *     '=' equals a old value
     *     
     *     * - - - *     = - - - =   = - * - =   = - = - =   = * = * =
     *     - - - - -     - - - - -   - - - - -   - * - * -   * = * = *       
     *     - - - - -     - - * - -   * - = - *   = - = - =   = * = * =
     *     - - - - -     - - - - -   - - - - -   - * - * -   * = * = *                
     *     * - - - *     = - - - =   = - * - =   = - = - =   = * = * =
     *         A             B           C           D           E
     *         
     *     A: Seed corners
     *     B: Randomized center value
     *     C: Diamond step
     *     D: Repeated square step
     *     E: Inner diamond step
     *     
     *     Rinse and repeat C->D->E until data map is filled
     *         
     ***************************************************************************/
    private static double[][] populateMap(double[][] map) {     

        // assures us we have a fresh map each time
        double avgOffSet = avgOffSetInit;

        // assigns the corners of the map values to SEED
        map[0][0] =
        map[0][MAP_SIZE-1] =
        map[MAP_SIZE-1][0] = 
        map[MAP_SIZE-1][MAP_SIZE-1] = SEED;

        // square and diamond loop start
        for(int sideLength = MAP_SIZE-1; sideLength >= 2; sideLength /=2,avgOffSet/= 2.0) {

            int halfSide = sideLength / 2;
            double avgOfPoints; 

            /********************************************************************
             *           [1]            SQUARE FRACTAL             [1]
             *********************************************************************/
            // loops through x & y values of the height map
            for(int x = 0; x < MAP_SIZE-1; x += sideLength) {
                for(int y = 0; y <MAP_SIZE-1; y += sideLength) {

                    avgOfPoints = map[x][y] +                 //top left point
                            map[x + sideLength][y] +            //top right point
                            map[x][y + sideLength] +            //lower left point
                            map[x + sideLength][y + sideLength];//lower right point

                    // average of surrounding points
                    avgOfPoints /= 4.0; 

                    // random value of 2*offset subtracted
                    // by offset for range of +/- the average
                    map[x+halfSide][y+halfSide] = avgOfPoints + 
                            (RAND.nextDouble()*2*avgOffSet) - avgOffSet;
                }
            }

            /********************************************************************
             *          [2]            DIAMOND FRACTAL           [2]
             *********************************************************************/
            for(int x=0; x < MAP_SIZE-1; x += halfSide) {
                for(int y = (x + halfSide) % sideLength; y < MAP_SIZE-1;

                        y += sideLength) {
                    avgOfPoints = 
                            map[(x - halfSide + MAP_SIZE) % MAP_SIZE][y] +//left of center
                            map[(x + halfSide) % MAP_SIZE][y] +           //right of center
                            map[x][(y + halfSide) % MAP_SIZE] +           //below center
                            map[x][(y - halfSide + MAP_SIZE) % MAP_SIZE]; //above center

                    // average of surrounding values
                    avgOfPoints /= 4.0; 

                    // in range of +/- offset
                    avgOfPoints += (RAND.nextDouble()*2*avgOffSet) - avgOffSet; 

                    //update value for center of diamond
                    map[x][y] = avgOfPoints;

                    // comment out for non wrapping values
                    if(x == 0)  map[MAP_SIZE-1][y] = avgOfPoints; 
                    if(y == 0)  map[x][MAP_SIZE-1] = avgOfPoints; 

                } // end y
            } // end x
        } // end of diamond
        return map;
    } // end of populateMap

    /*************************************************************************************
     * @param requires a 2d map array to print the values of at +/-0.00
     ************************************************************************************/
    @SuppressWarnings("unused")
    private static void printMap(double[][] map) {
        System.out.println("---------------------------------------------");     

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                System.out.printf("%+.2f ", map[x][y] );              
            }
            System.out.println(); 
        }       
    }

} // end of class