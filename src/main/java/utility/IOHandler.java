package utility;

import org.eclipse.collections.impl.list.mutable.FastList;
import wblut.geom.WB_CoordCollection;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Point;
import wblut.hemesh.HEC_FromFacelist;
import wblut.hemesh.HE_Mesh;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * handle input/output issues
 *
 * @author Wu
 * @create 2021-03-26 8:52
 */

public class IOHandler {
    public static final WB_GeometryFactory gf = WB_GeometryFactory.instance();

    public static List<HE_Mesh> readFromOBJFile(String path) {
        List<HE_Mesh> meshes = new ArrayList<>();
        if (path == null) {
            return meshes;
        }

        final File file = new File(path);
        List<WB_Point> vertexList = new FastList<>();
        List<WB_Point> UVWList = new FastList<>();
        List<int[]> faceList = new FastList<>();
        List<int[]> faceUVWList = new FastList<>();
        int faceCount = 0;
        boolean hasTexture = false;
        HEC_FromFacelist creator;
        try (InputStream is = createInputStream(file)) {
            assert is != null;
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // split every line in parts divided by spaces
                    final String[] parts = line.split("\\s+");
                    // the first part indicates the kind of data that is in that line
                    // g stands for group
                    if (parts[0].equals("g")) {
                        if (faceCount != 0) {
                            // the HEC_FromFacelist wants the face data as int[][]
                            final int[][] faceArray = new int[faceCount][];
                            for (int i = 0; i < faceCount; i++) {
                                final int[] tempFace = faceList.get(i);
                                faceArray[i] = tempFace;
                            }
                            final int[][] faceUVWArray = new int[faceCount][];
                            if (hasTexture) {
                                for (int i = 0; i < faceCount; i++) {
                                    final int[] tempUVWFace = faceUVWList.get(i);
                                    faceUVWArray[i] = tempUVWFace;
                                }
                            }

                            // et voila... add to the creator
                            creator = new HEC_FromFacelist();
                            creator.setVertices(vertexList);
                            creator.setFaces(faceArray);
                            if (hasTexture) {
                                creator.setFacesUVW(faceUVWArray);
                                creator.setFaceVertexUVW(UVWList);
                            }
                            meshes.add(new HE_Mesh(creator));
                            faceList = new FastList<>();
                            faceUVWList = new FastList<>();
                            faceCount = 0;
                        }

                    }
                    // v stands for vertex data
                    if (parts[0].equals("v")) {
                        final double x1 = Double.parseDouble(parts[1]);
                        final double y1 = Double.parseDouble(parts[2]);
                        final double z1 = parts.length > 3
                                ? Double.parseDouble(parts[3])
                                : 0;
                        final WB_Point pointLoc = new WB_Point(x1, y1, z1);
                        vertexList.add(pointLoc);
                    }
                    if (parts[0].equals("vt")) {
                        final double u = Double.parseDouble(parts[1]);
                        final double v = parts.length > 2
                                ? Double.parseDouble(parts[2])
                                : 0;
                        final double w = parts.length > 3
                                ? Double.parseDouble(parts[3])
                                : 0;
                        final WB_Point pointUVW = new WB_Point(u, v, w);
                        UVWList.add(pointUVW);
                        hasTexture = true;
                    }
                    // f stands for facelist data
                    // should work for non triangular faces
                    if (parts[0].equals("f")) {
                        final int[] tempFace = new int[parts.length - 1];
                        final int[] tempUVWFace = new int[parts.length - 1];
                        for (int j = 0; j < parts.length - 1; j++) {
                            final String[] num = parts[j + 1].split("/");
                            tempFace[j] = Integer.parseInt(num[0]) - 1;
                            if (num.length > 2) {
                                tempUVWFace[j] = Integer.parseInt(num[2]) - 1;
                            }
                        }
                        faceList.add(tempFace);
                        faceUVWList.add(tempUVWFace);
                        faceCount++;
                    }
                }

                if (faceCount != 0) {
                    // the HEC_FromFacelist wants the face data as int[][]
                    final int[][] faceArray = new int[faceCount][];
                    for (int i = 0; i < faceCount; i++) {
                        final int[] tempFace = faceList.get(i);
                        faceArray[i] = tempFace;
                    }
                    final int[][] faceUVWArray = new int[faceCount][];
                    if (hasTexture) {
                        for (int i = 0; i < faceCount; i++) {
                            final int[] tempUVWFace = faceUVWList.get(i);
                            faceUVWArray[i] = tempUVWFace;
                        }
                    }

                    creator = new HEC_FromFacelist();
                    creator.setVertices(vertexList);
                    creator.setFaces(faceArray);
                    if (hasTexture) {
                        creator.setFacesUVW(faceUVWArray);
                        creator.setFaceVertexUVW(UVWList);
                    }
                    meshes.add(new HE_Mesh(creator));
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return meshes;
    }

    public static HE_Mesh switchObjYZ(HE_Mesh mesh) {
        WB_CoordCollection coords = mesh.getPoints();
        int[][] faces = mesh.getFacesAsInt();
        List<WB_Point> pts = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            WB_Point pt = new WB_Point(coords.get(i));
            pt = new WB_Point(pt.xd(), -pt.zd(), pt.yd());
            pts.add(pt);
        }

        return new HE_Mesh(gf.createMesh(pts, faces));
    }

    private static InputStream createInputStream(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("file can't be null");
        }
        try {
            InputStream stream = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                stream = new GZIPInputStream(stream);
            }
            return stream;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
