package os.hw1.programs;

import os.hw1.util.Logger;

import java.util.Scanner;

import static os.hw1.Tester.WAIT_P1;

public class Program1 {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Logger.getInstance().log("Program1 is callleeedddd!!!");
        Thread.sleep(WAIT_P1 - 50);
        System.out.println(scanner.nextInt() - 1);
    }
}
