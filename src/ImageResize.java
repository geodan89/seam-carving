import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stopwatch;

public class ImageResize {
    public static Picture resize(Picture inputImage, int width, int height) {
        if (width >= inputImage.width() || height >= inputImage.height()) {
            throw new IllegalArgumentException("Enter valid width and height");
        }
        int removeColumns = inputImage.width() - width;
        int removeRows = inputImage.height() - height;

        SeamCarving sc = new SeamCarving(inputImage);

        Stopwatch sw = new Stopwatch();

        for (int i = 0; i < removeColumns; i++) {
            int[] verticalSeam = sc.findVerticalSeam();
            sc.removeVerticalSeam(verticalSeam);
        }

        for (int i = 0; i < removeRows; i++) {
            int[] horizontalSeam = sc.findHorizontalSeam();
            sc.removeHorizontalSeam(horizontalSeam);
        }

        Picture outputImage = sc.picture();
        StdOut.println("Resizing time: " + sw.elapsedTime() + " seconds.");
        return outputImage;
    }
}
