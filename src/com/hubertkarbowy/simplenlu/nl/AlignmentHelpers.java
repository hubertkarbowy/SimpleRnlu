package com.hubertkarbowy.simplenlu.nl;

public class AlignmentHelpers {

    public enum DistanceType {EDIT, LEVENSHTEIN}

    public static int editDistance(String a, String b, DistanceType distanceType) {
        String x = "#" + a;
        String y = "#" + b;
        int n = x.length();
        int m = y.length();
        int[][] distance = new int[n][m];

        // distance[i,j] = edit distance between the first i characters of X and the first j characters of Y
        for (int i=0; i<distance[0].length; i++) distance[0][i] = i;
        for (int j=0; j<distance.length; j++) distance[j][0] = j;

        for (int j=1; j<y.length(); j++) {
            for (int i = 1; i < x.length(); i++) {
                int deletion = distance[i-1][j] + 1;
                int substitution = distance[i][j-1] + 1;
                int insertion = distance[i-1][j-1] + (x.charAt(i) != y.charAt(j) ?
                        (distanceType == DistanceType.LEVENSHTEIN) ? 1 : 2 : 0 );
                distance[i][j] = Math.min(Math.min(deletion,substitution),insertion);
            }
        }

//        for (int i=0; i<n; i++) {
//            for (int j=0; j<m; j++) {
//                System.out.print(""  + distance[i][j] + "\t");
//            }
//            System.out.println();
//        }
//        System.out.println("The Levenshtein difference between \"" + a + "\" and \"" + b + "\" is " + distance[n-1][m-1]);
    return distance[n-1][m-1];
    }
}
