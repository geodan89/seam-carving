import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.io.File;

public class SeamCarving {
    private static final int BORDER_ENERGY = 1000;
    private static final int COLOR_CONSTANT = 0xFF;
    private static final boolean HORIZONTAL = true;
    private static final boolean VERTICAL = false;
    private Picture picture;
    private double[] distanceToPixel;
    private int[] edgeTo;
    private double[][] energyMatrix;

    // create a seam carver object based on the given picture
    public SeamCarving(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Picture not found!");
        }
        this.picture = picture;
    }

    /**
     * Current picture.
     *
     * @return the current picture.
     */
    public Picture picture() {
        return this.picture;
    }

    /**
     * Width of current picture.
     *
     * @return the width of the current picture.
     */
    public int width() {
        return this.picture.width();
    }

    /**
     * Height of current picture.
     *
     * @return the height of the current picture.
     */
    public int height() {
        return this.picture.height();
    }

    /**
     * Calculates energy of a pixel at column x and row y.
     *
     * @param col
     * @param row
     * @return energy of a pixel
     */
    public double energy(int col, int row) {
        energyMatrix = new double[width()][height()];
        if (col >= width() || row >= height()
                || col < 0 || row < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (col == width() - 1 || row == height() - 1
                || col == 0 || row == 0) {
            return (double) BORDER_ENERGY;
        }
        if (energyMatrix[col][row] != 0.0) {
            return energyMatrix[col][row];
        }
        return Math.sqrt(xgradient(col, row) + ygradient(col, row));
    }

    private double energyOfPixel(int pixel) {
        return energy(pixelColumn(pixel), pixelRow(pixel));
    }

    /**
     * Returns red value.
     *
     * @param rgb value
     * @return red value
     */
    private int getRed(int rgb) {
        return (rgb >> 16) & COLOR_CONSTANT;
    }

    /**
     * Returns green value
     *
     * @param rgb
     * @return green value
     */
    private int getGreen(int rgb) {
        return (rgb >> 8) & COLOR_CONSTANT;
    }

    /**
     * Returns blue value.
     *
     * @param rgb
     * @return blue value
     */
    private int getBlue(int rgb) {
        return (rgb >> 0) & COLOR_CONSTANT;
    }

    /**
     * Calculates gradient with respect to column.
     *
     * @param col
     * @param row
     * @return gradient
     */
    private double xgradient(int col, int row) {
        int left = picture.getRGB(col - 1, row);
        int right = picture.getRGB(col + 1, row);
        int red = getRed(right) - getRed(left);
        int green = getGreen(right) - getGreen(left);
        int blue = getBlue(right) - getBlue(left);
        return (red * red) + (green * green) + (blue * blue);
    }

    /**
     * Calculates gradient with respect to row.
     *
     * @param col
     * @param row
     * @return gradient
     */
    private double ygradient(int col, int row) {
        int up = picture.getRGB(col, row - 1);
        int down = picture.getRGB(col, row + 1);
        int red = getRed(up) - getRed(down);
        int green = getGreen(up) - getGreen(down);
        int blue = getBlue(up) - getBlue(down);
        return (red * red) + (green * green) + (blue * blue);
    }

    /**
     * Transposes picture from horizontal to vertical or visa versa.
     */
    private void transpose() {
        Picture transposedPicture = new Picture(picture.height(), picture.width());
        double[][] transposedPictureEnergyMatrix = new double[picture.height()][picture.width()];
        for (int col = 0; col < picture.width(); col++) {
            for (int row = 0; row < picture.height(); row++) {
                transposedPicture.set(row, col, picture.get(col, row));
                transposedPictureEnergyMatrix[row][col] = energyMatrix[col][row];
            }
        }
        energyMatrix = transposedPictureEnergyMatrix;
        picture = transposedPicture;
    }

    /**
     * Returns array of length Height, having as elements the pixels column indices.
     * Save the energies in a local variable energy[][] and access the information
     * directly from the 2D array (instead of recomputing from scratch).
     *
     * @return the sequence of indices for the vertical seam.
     */
    public int[] findVerticalSeam() {
        int[] verticalSeam = new int[height()];
        energyMatrix = new double[width()][height()];
        distanceToPixel = new double[width() * height()];
        edgeTo = new int[width() * height()];


        //set distance to top pixels to 0 and everything else to infinity
        //set energies in the energy matrix
        for (int col = 0; col < width(); col++) {
            for (int row = 0; row < height(); row++) {
                int pixel = pixelNumber(col, row);
                if (row == 0) {
                    distanceToPixel[pixel] = 0.0;
                } else {
                    distanceToPixel[pixel] = Double.POSITIVE_INFINITY;
                }
                edgeTo[pixel] = -1;
                energyMatrix[col][row] = energy(col, row);
            }
        }
        //relax everything
        for (int row = 0; row < height() - 1; row++) {
            for (int col = 0; col < width(); col++) {
                //relax bottom left
                if (col - 1 >= 0) {
                    relax(col, row, col - 1, row + 1);
                }
                //relax bottom
                relax(col, row, col, row + 1);
                //relax bottom right
                if (col + 1 < width()) {
                    relax(col, row, col + 1, row + 1);
                }
            }
        }
        //find min value of last line and record x value
        double colMinEnergy = Double.POSITIVE_INFINITY;
        int lastSeamPixel = -1;
        for (int col = 0; col < width(); col++) {
            int pixel = pixelNumber(col, height() - 1);
            if (distanceToPixel[pixel] < colMinEnergy) {
                colMinEnergy = distanceToPixel[pixel];
                lastSeamPixel = pixel;
            }
        }
        //go back and find seam
        for (int pixel = lastSeamPixel; pixel >= 0; pixel = edgeTo[pixel]) {
            int row = pixelRow(pixel);
            int column = pixelColumn(pixel);
            verticalSeam[row] = column;
        }
        return verticalSeam;
    }

    /**
     * Finds horizontal seam.
     *
     * @return the sequence of indices for the horizontal seam.
     */
    public int[] findHorizontalSeam() {
        transpose();
        int[] horizontalSeam = findVerticalSeam();
        transpose();
        return horizontalSeam;
    }

    /**
     * Relaxation as per Dijkstra's algorithm
     *
     * @return the sequence of indices for the horizontal seam.
     */
    private void relax(int col1, int row1, int col2, int row2) {
        int pixel1 = pixelNumber(col1, row1);
        int pixel2 = pixelNumber(col2, row2);
        if (distanceToPixel[pixel2] > distanceToPixel[pixel1] + energyOfPixel(pixel2)) {
            distanceToPixel[pixel2] = distanceToPixel[pixel1] + energyOfPixel(pixel2);
            edgeTo[pixel2] = pixel1;
        }
    }

    /**
     * Remove vertical seam
     */
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        transpose();
        removeVerticalSeam(seam);
        transpose();
    }

    /**
     * Remove vertical seam
     */
    public void removeVerticalSeam(int[] seam) {
        // validate seam
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        if (width() <= 1) {
            throw new IllegalArgumentException();
        }

        if (seam.length != height()) {
            throw new IllegalArgumentException();
        }
        //create new picture without seam
        Picture updatedPicture = new Picture(width() - 1, height());
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < seam[row]; col++) {
                updatedPicture.set(col, row, this.picture.get(col, row));
            }
            for (int col = seam[row] + 1; col < width(); col++) {
                updatedPicture.set(col - 1, row, this.picture.get(col - 1, row));
            }
        }

        this.picture = updatedPicture;

        energyMatrix = new double[picture.width()][picture.height()];
        for (int col = 0; col < width(); col++) {
            for (int row = 0; row < height(); row++) {
                energyMatrix[col][row] = energy(col, row);
            }
        }
    }

    private int pixelRow(int pixel) {
        return pixel / width();
    }

    private int pixelColumn(int pixel) {
        return pixel % width();
    }

    private int pixelNumber(int col, int row) {
        return (width() * row + col);
    }


    private static void printSeam(SeamCarving carver, int[] seam, boolean direction) {
        double totalSeamEnergy = 0.0;

        for (int row = 0; row < carver.height(); row++) {
            for (int col = 0; col < carver.width(); col++) {
                double energy = carver.energy(col, row);
                String marker = " ";
                if ((direction == HORIZONTAL && row == seam[col]) ||
                        (direction == VERTICAL && col == seam[row])) {
                    marker = "*";
                    totalSeamEnergy += energy;
                }
                StdOut.printf("%7.2f%s ", energy, marker);
            }
            StdOut.println();
        }
        StdOut.printf("Total energy = %f\n", totalSeamEnergy);
        StdOut.println();
        StdOut.println();
    }

    // unit testing (optional)
    public static void main(String[] args) {
        String filePath = "/Users/geodan/Documents/seam-carving/images-for-testing/";
        String pictureName = "10x12.png";
        File imageFile = new File(filePath + pictureName);
        Picture picture = new Picture(imageFile);
        StdOut.printf("%s (%d-by-%d image)\n", imageFile, picture.width(), picture.height());
        StdOut.println();
        StdOut.println("The table gives the dual-gradient energies of each pixel.");
        StdOut.println("The asterisks denote a minimum energy vertical or horizontal seam.");
        StdOut.println();

        SeamCarving carver = new SeamCarving(picture);


        StdOut.printf("Vertical seam: { ");
        int[] verticalSeam = carver.findVerticalSeam();
        for (int x : verticalSeam)
            StdOut.print(x + " ");
        StdOut.println("}");
        printSeam(carver, verticalSeam, VERTICAL);

        StdOut.printf("Horizontal seam: { ");
        int[] horizontalSeam = carver.findHorizontalSeam();
        for (int y : horizontalSeam)
            StdOut.print(y + " ");
        StdOut.println("}");
        printSeam(carver, horizontalSeam, HORIZONTAL);

        Picture resizedPic = ImageResize.resize(picture, 4, 5);
        File resizedFilePath = new File(filePath + "Resized" + pictureName);
        resizedPic.save(resizedFilePath);
        StdOut.printf("new image size is %d columns by %d rows\n", resizedPic.width(), resizedPic.height());
        StdOut.printf("%s (%d-by-%d image)\n", resizedFilePath, picture.width(),
                picture.height());


    }


}

