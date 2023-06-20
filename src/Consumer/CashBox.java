package Consumer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class CashBox extends AtomicReference<CashBox> {
  public static int TIME_TO_NEXT = 1;
  public int timeOf = 1;
  int queueLen;
  int speed;
  public CashBoxType type;
  int maxLength;
  private static final Random RANDOM = new Random();

  public int getMaxLength() {
    return maxLength;
  }

  public static ArrayList<CashBox> FASTS = new ArrayList<>();
  public static ArrayList<CashBox> STANDARDS = new ArrayList<>();
  public static int STANDARD_CASH_BOX_SPEED = 20;
  public static int FAST_CASH_BOX_SPEED = 10;

  public CashBox(CashBoxType type) {
    this.queueLen = 0;
    this.type = type;
    this.maxLength = type == CashBoxType.STANDARD ? 10 : 5;
    this.speed = type == CashBoxType.STANDARD ? STANDARD_CASH_BOX_SPEED : FAST_CASH_BOX_SPEED;
  }

  public CashBox() {
  }

  public int getQueueLen() {
    return queueLen;
  }

  public void incQueue() {
    queueLen++;
    maxLength = type == CashBoxType.STANDARD ? 15 : 10;
    if (queueLen > maxLength) {
//      System.out.println("Kolejka w jednej z kas typu " + type + " jest za długa(" + queueLen + ")");
      CashBoxFederate.RUNNING = false;
      throw new RuntimeException("Kolejka w jednej z kas typu " + type + " jest za długa(" + queueLen + ")");
    }
  }

  static void decQueues(CashBoxType cashBoxType) {
    if (CashBoxType.STANDARD == cashBoxType) {
      STANDARDS.forEach(cashBox -> {
        if (cashBox.queueLen > 0) {
          cashBox.decQueue(cashBox);
        }
      });
    } else {
      FASTS.forEach(cashBox -> {
        if (cashBox.queueLen > 0) {
          cashBox.decQueue(cashBox);
        }
      });
    }
  }

  public void decQueue(CashBox cashBox) {
    if (queueLen > 0) {
      queueLen--;
      System.out.println("\tObsłużono klienta z kasy typu " + cashBox.type);
    } else {
      System.out.println("\tKolejka już była pusta więc nikogo nie obsłużono");
    }
  }

  public int consume() {
    TIME_TO_NEXT = generateTimeToNext();
    int count = RANDOM.nextInt(15) + 1;
    System.out.println("I want to consume " + count + ". Next I'll be consuming in " + TIME_TO_NEXT);
    return count;
  }

  public int getTimeToNext() {
    return TIME_TO_NEXT;
  }

  private int generateTimeToNext() {
    return RANDOM.nextInt(10) + 1;
  }

  public static void setTimeToNext(int timeToNext) {
    TIME_TO_NEXT = timeToNext;
  }

  public void setQueueLen(int queueLen) {
    this.queueLen = queueLen;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public void setType(CashBoxType type) {
    this.type = type;
  }

  public void setType(int type) {
    this.type = type == 0 ? CashBoxType.STANDARD : CashBoxType.FAST;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }
}
