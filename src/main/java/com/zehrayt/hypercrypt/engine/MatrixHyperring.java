package com.zehrayt.hypercrypt.engine;

// Apache Commons Math kütüphanesinden gerekli sınıfları import ediyoruz.
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.HashSet;
import java.util.Set;

/**
 * Bu sınıf, elemanları matrisler olan ve hiper-toplama işlemi
 * A ⊕ B = { A + B, A - B } olarak tanımlanan bir hiperhalkayı modeller.
 * Bu, Dergipark'taki makalelerde bulunan yaygın bir yapıdır.
 */
public class MatrixHyperring {

    /**
     * İki matrisi, A ⊕ B = { A + B, A - B } kuralına göre hiper-toplar.
     * Bu metot, Apache Commons Math kütüphanesinin RealMatrix arayüzünü kullanır.
     * 
     * @param a Birinci matris (A)
     * @param b İkinci matris (B)
     * @return Hiper-toplama sonucunda oluşan {A+B, A-B} matrisler kümesi.
     */
    public Set<RealMatrix> hyperAdd(RealMatrix a, RealMatrix b) {
        // Boyutların uyumlu olup olmadığını kontrol et.
        if (a.getRowDimension() != b.getRowDimension() || a.getColumnDimension() != b.getColumnDimension()) {
            throw new IllegalArgumentException("Matrices must have the same dimensions for addition.");
        }

        // Sonuçları tutacağımız HashSet'i oluşturuyoruz.
        Set<RealMatrix> resultSet = new HashSet<>();

        System.out.println("Performing hyper-addition for matrices: A ⊕ B = {A+B, A-B}");

        // Kural 1: A + B
        RealMatrix sum = a.add(b);
        resultSet.add(sum);
        System.out.println("Added A+B to the result set.");

        // Kural 2: A - B
        RealMatrix difference = a.subtract(b);
        resultSet.add(difference);
        System.out.println("Added A-B to the result set.");

        return resultSet;
    }
    
    /**
     * Klasik matris çarpımını uygular. Bu örnekte çarpma işlemi
     * bir hiper-işlem değildir, klasik bir işlemdir.
     * 
     * @param a Birinci matris
     * @param b İkinci matris
     * @return A * B çarpım matrisi
     */
    public RealMatrix multiply(RealMatrix a, RealMatrix b) {
        System.out.println("Performing standard matrix multiplication: A * B");
        return a.multiply(b);
    }


    // --- Test ve Gösterim İçin Main Metodu ---
    public static void main(String[] args) {
        MatrixHyperring engine = new MatrixHyperring();
        
        // 2x2'lik iki örnek matris oluşturalım.
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        
        RealMatrix matrixA = new Array2DRowRealMatrix(dataA);
        RealMatrix matrixB = new Array2DRowRealMatrix(dataB);
        
        System.out.println("Matrix A:\n" + matrixA);
        System.out.println("Matrix B:\n" + matrixB);
        
        System.out.println("\n--- Testing Hyper-Addition ---");
        Set<RealMatrix> additionResult = engine.hyperAdd(matrixA, matrixB);
        
        System.out.println("\nHyper-addition result set (A ⊕ B) contains " + additionResult.size() + " matrices:");
        for (RealMatrix matrix : additionResult) {
            System.out.println("---");
            System.out.println(matrix);
        }

        System.out.println("\n--- Testing Standard Multiplication ---");
        RealMatrix multiplicationResult = engine.multiply(matrixA, matrixB);
        System.out.println("\nMultiplication result (A * B):\n" + multiplicationResult);
    }
}