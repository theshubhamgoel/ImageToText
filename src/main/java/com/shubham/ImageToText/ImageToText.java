package com.shubham.ImageToText;

//https://www.sumopaint.com/home/#app
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.imageio.ImageIO;

public class ImageToText {

    static Pixel[][] matrix;
    static int value[] = { 574, 753, 545, 685, 633 };

    public static void main(String[] args) throws Exception {

        String folder = "/Users/shubhgoe/Desktop/extra/sample/";
        File input = new File(folder + "5.png");
        BufferedImage image = ImageIO.read(input);

        matrix = new Pixel[image.getHeight()][image.getWidth()];
        Pixel[][] tempMatrix = new Pixel[image.getHeight()][image.getWidth()];

        // fill pixel matrix
        fillGreyScaleImagePixelMatrix(matrix, image);
        fillBackup(tempMatrix);
        // printImage(matrix);

        Properties properties = loadPropertyFile();
        // printProperties(properties);
        // System.out.println(properties.size());

        String ans = "";
        // read all the letter one by one
        int x = 0;
        while (true) {
            Position p = getStartingPoint();
            if (p == null) {
                break;
            }

            System.out.println(p);
            x++;

            LetterBox letterBox = new LetterBox(matrix.length, 0, matrix[0].length, 0);
            getLetterBox(p, letterBox);
            // System.out.println(letterBox);

            // Special case for I
            if ((letterBox.right - letterBox.left) != 0
                    && (letterBox.bottom - letterBox.top) / (letterBox.right - letterBox.left) > 5) {
                ans += "I";
                continue;
            }
            int ninePointer[] = new int[9];
            ninePointer = getNinePointerMatrix(letterBox, tempMatrix, ninePointer);

            letterBox.top += 1;
            ninePointer = getNinePointerMatrix(letterBox, tempMatrix, ninePointer);

            letterBox.left += 1;
            ninePointer = getNinePointerMatrix(letterBox, tempMatrix, ninePointer);

            letterBox.right -= 1;
            ninePointer = getNinePointerMatrix(letterBox, tempMatrix, ninePointer);

            String alphabetCode = getAlphabetCode(ninePointer);
            System.out.println(alphabetCode);
            String alphabet = getAlphabet(alphabetCode, properties);

            // Special Cases for H and N
            if (alphabet.equals("N")) {
                int rowMiddle = (letterBox.top + letterBox.bottom) / 2;
                if (isHorizontolLine(rowMiddle, letterBox.left, letterBox.right, tempMatrix)) {
                    alphabet = "H";
                }
            }

            // Special Cases for G and O
            if (alphabet.equals("O")) {
                int rowMiddle = (letterBox.top + letterBox.bottom) / 2;
                if (!isConnectedArc(rowMiddle, letterBox.right + 1, letterBox.top, tempMatrix)) {
                    alphabet = "G";
                }
            }

            ans += alphabet;

            createBoundrary(letterBox, tempMatrix);
        }
        // System.out.println(x);
        System.out.println("Sir we found some text in the image : " + ans);

        updateImage(tempMatrix, image);
        File ouptut = new File(folder + "grayscale.png");
        ImageIO.write(image, "png", ouptut);

    }

    private static boolean isConnectedArc(int row, int col, int top, Pixel[][] tempMatrix) {
        for (int i = row; i > top; i--) {
            if (tempMatrix[i - 1][col].avg == 0) {
                // do nothing
            } else if (tempMatrix[i - 1][col + 1].avg == 0) {
                col--;
            } else if (tempMatrix[i - 1][col - 1].avg == 0) {
                col--;
            } else if (tempMatrix[i - 1][col - 2].avg == 0) {
                col -= 2;
            } else if (tempMatrix[i - 1][col - 3].avg == 0) {
                col -= 3;
            } else if (tempMatrix[i - 1][col - 4].avg == 0) {
                col -= 4;
            } else {
                return false;
            }

            while (tempMatrix[i - 1][col].avg == 0) {
                col++;
            }
        }
        return true;

    }

    private static boolean isHorizontolLine(int row, int left, int right, Pixel[][] tempMatrix) {
        for (int i = left; i < right; i++) {
            if (tempMatrix[row][i].avg != 0)
                return false;
        }
        return true;
    }

    private static String getAlphabetCode(int[] ninePointer) {
        String x = "";
        for (int i = 0; i < ninePointer.length; i++) {
            x += ninePointer[i] != 0 ? "" + (i + 1) : "";
        }
        return x;
    }

    private static String getAlphabet(String alphabetCode, Properties prop) {
        return prop.get(alphabetCode) == null ? "" : (String) prop.get(alphabetCode);
    }

    private static int[] getNinePointerMatrix(LetterBox letterBox, Pixel[][] tempMatrix, int[] ninePointer) {
        int rowMiddle = (letterBox.top + letterBox.bottom) / 2;
        int colMiddle = (letterBox.left + letterBox.right) / 2;
        ninePointer[0] += tempMatrix[letterBox.top][letterBox.left].avg == 0 ? 1 : 0;
        ninePointer[1] += tempMatrix[letterBox.top][colMiddle].avg == 0 ? 1 : 0;
        ninePointer[2] += tempMatrix[letterBox.top][letterBox.right].avg == 0 ? 1 : 0;
        ninePointer[3] += tempMatrix[rowMiddle][letterBox.left].avg == 0 ? 1 : 0;
        ninePointer[4] += tempMatrix[rowMiddle][colMiddle].avg == 0 ? 1 : 0;
        ninePointer[5] += tempMatrix[rowMiddle][letterBox.right].avg == 0 ? 1 : 0;
        ninePointer[6] += tempMatrix[letterBox.bottom][letterBox.left].avg == 0 ? 1 : 0;
        ninePointer[7] += tempMatrix[letterBox.bottom][colMiddle].avg == 0 ? 1 : 0;
        ninePointer[8] += tempMatrix[letterBox.bottom][letterBox.right].avg == 0 ? 1 : 0;

        return ninePointer;
    }

    private static void printProperties(Properties prop) {
        Enumeration<?> e = prop.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = prop.getProperty(key);
            System.out.println("Key : " + key + ", Value : " + value);
        }
    }

    private static Properties loadPropertyFile() throws Exception {
        Properties prop = new Properties();
        prop.load(new FileInputStream("resources/Letters.properties"));
        return prop;
    }

    private static void updateImage(Pixel[][] tempMatrix, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image.setRGB(j, i, tempMatrix[i][j].p);
            }
        }
    }

    private static void fillBackup(Pixel[][] backupMatrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                backupMatrix[i][j] = new Pixel(matrix[i][j].p, matrix[i][j].avg);
            }
        }
    }

    private static void createBoundrary(LetterBox letterBox, Pixel[][] tempMatrix) {
        for (int i = letterBox.left; i < letterBox.right; i++) {
            int pixel = 0;
            tempMatrix[letterBox.top][i].p = pixel;
            tempMatrix[letterBox.top][i].avg = 0;

            pixel = (tempMatrix[letterBox.bottom][i].p << 24);
            tempMatrix[letterBox.bottom][i].p = pixel;
            tempMatrix[letterBox.bottom][i].avg = 0;
        }

        for (int i = letterBox.top; i < letterBox.bottom; i++) {
            int pixel = 0;
            tempMatrix[i][letterBox.left].p = pixel;
            tempMatrix[i][letterBox.left].avg = 0;

            pixel = (tempMatrix[letterBox.bottom][i].p << 24);
            tempMatrix[i][letterBox.right].p = pixel;
            tempMatrix[i][letterBox.right].avg = 0;
        }
    }

    private static String getLetter(int count) {
        String x = "";
        for (int i = 0; i < value.length; i++) {
            if (Math.abs(count - value[i]) < 2) {
                x = Character.toString((char) (i + 65));
            }
        }

        return x;
    }

    private static Position getStartingPoint() {
        Position p = null;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j].avg == 0) {

                    for (int j2 = 0; j2 < j; j2++) {
                        if (matrix[i + 1][j2].avg == 0) {
                            p = new Position(i + 1, j2);
                            return p;
                        }
                    }
                    p = new Position(i, j);
                    return p;
                }
            }
        }
        return p;
    }

    private static void getLetterBox(Position p, LetterBox letterBox) {
        int width = matrix[0].length;
        int height = matrix.length;

        if (p.i < 0 || p.i >= height)
            return;
        if (p.j < 0 || p.j >= width)
            return;

        Pixel pixel = matrix[p.i][p.j];
        if (pixel.avg == 0) {
            pixel.avg = 2;

            letterBox.top = p.i < letterBox.top ? p.i : letterBox.top;
            letterBox.bottom = p.i > letterBox.bottom ? p.i : letterBox.bottom;
            letterBox.left = p.j < letterBox.left ? p.j : letterBox.left;
            letterBox.right = p.j > letterBox.right ? p.j : letterBox.right;

            getLetterBox(new Position(p.i, p.j + 1), letterBox);
            getLetterBox(new Position(p.i + 1, p.j), letterBox);
            getLetterBox(new Position(p.i, p.j - 1), letterBox);
            getLetterBox(new Position(p.i - 1, p.j), letterBox);
        } else {
            return;
        }
    }

    private static void printImage(Pixel[][] matrix) {

        System.out.println("Image height : " + matrix.length);
        System.out.println("Image width : " + matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                Pixel p = matrix[i][j];
                System.out.print(p.avg + "");
            }
            System.out.println();
        }
    }

    private static void fillGreyScaleImagePixelMatrix(Pixel[][] matrix, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int p = image.getRGB(j, i);

                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                // calculate average
                int avg = (r + g + b) / 3;

                // round off
                avg = avg > 100 ? 255 : 0;

                // replace RGB value with avg
                p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                image.setRGB(j, i, p);

                matrix[i][j] = new Pixel(p, avg);
            }
        }

    }
}
