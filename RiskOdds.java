/*
 * RiskOdds.java
 *
 * Copyright (c) 2009-2020 Michael Banack <bob5972@banack.net>
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.ArrayList;
import java.util.HashMap;
 
public class RiskOdds
{
	private static final boolean DEBUG=false;
	private static final boolean DEBUG_PERMUTATION=DEBUG&&true;
	
	private static boolean amInitialized=false;
	private static HashMap winOddsCache=new HashMap();
	private static HashMap expectedWinValueCache = new HashMap();
	
	private static double[][][][] totalProb;
	private static double[][] attackExpectedValue;
	private static double[][] defenseExpectedValue;
	
	
	private void checkDiceValid(int atkDice,int defDice)
	{
		if(atkDice > 3 || defDice >2 || atkDice < 0 || defDice < 0)
			throw new IllegalArgumentException("Invalid Dice!");
	}
	
	public double getOdds(int atkDice,int defDice,int atkLoss,int defLoss)
	{
		checkDiceValid(atkDice,defDice);
		if(defLoss>2 || atkLoss > 2 || defLoss<0 || atkLoss<0)
			return 0;
		return totalProb[atkDice][defDice][atkLoss][defLoss];
	}
	
	public double getDefLossOdds(int atkDice, int defDice, int defLoss)
	{
		checkDiceValid(atkDice,defDice);
		if(defLoss > 2)
			return 0;
		double oup = 0;
		for(int x=0;x<4;x++)
		{
			oup += totalProb[atkDice][defDice][x][defLoss];
		}
		return oup;
	}
	public double getAtkLossOdds(int atkDice, int defDice, int atkLoss)
	{
		checkDiceValid(atkDice,defDice);
		if(atkLoss > 2)
			return 0;
		double oup = 0;
		for(int x=0;x<4;x++)
		{
			oup += totalProb[atkDice][defDice][atkLoss][x];
		}
		return oup;
	}
	
	public double getAtkExpectedValue(int atkDice,int defDice)
	{
		checkDiceValid(atkDice,defDice);
		return attackExpectedValue[atkDice][defDice];
	}
	
	public double getDefExpectedValue(int atkDice,int defDice)
	{
		checkDiceValid(atkDice,defDice);
		return defenseExpectedValue[atkDice][defDice];
	}
	
	public double getWinOdds(int atkSize, int defSize)
	{
		if(atkSize <= 1)
			return 0;
		if(defSize <= 0)
			return 1;
		
		if(winOddsCache.containsKey(new IntPair(atkSize,defSize)))
		{
			return ((Double)winOddsCache.get(new IntPair(atkSize,defSize))).doubleValue();
		}
		
		int atkDice,defDice;
		if(atkSize >= 4)
			atkDice=3;
		else if(atkSize == 3)
			atkDice=2;
		else if(atkSize == 2)
			atkDice=1;
		else
			throw new IllegalArgumentException("Bad attack army size!");
		
		if(defSize>=2)
			defDice = 2;
		else if(defSize == 1)
			defDice=1;
		else
			throw new IllegalArgumentException("Bad defense army size!");
		
		double oup = 0;
		
		for(int defLoss=0;defLoss<=2;defLoss++)
		{
			for(int atkLoss=0;atkLoss<=2;atkLoss++)
			{
				if(getOdds(atkDice,defDice,atkLoss,defLoss)!= 0)
					oup += getOdds(atkDice,defDice,atkLoss,defLoss)*getWinOdds(atkSize-atkLoss,defSize-defLoss);
			}
		}
		winOddsCache.put(new IntPair(atkSize,defSize), new Double(oup));
		return oup;		
	}
	
	public double getExpectedWinValue(int atkSize,int defSize)
	{
		if(atkSize <= 1)
			return 0;
		if(defSize <= 0)
			return atkSize;
		
		if(expectedWinValueCache.containsKey(new IntPair(atkSize,defSize)))
		{
			return ((Double)expectedWinValueCache.get(new IntPair(atkSize,defSize))).doubleValue();
		}
		
		int atkDice,defDice;
		if(atkSize >= 4)
			atkDice=3;
		else if(atkSize == 3)
			atkDice=2;
		else if(atkSize == 2)
			atkDice=1;
		else
			throw new IllegalArgumentException("Bad attack army size!");
		
		if(defSize>=2)
			defDice = 2;
		else if(defSize == 1)
			defDice=1;
		else
			throw new IllegalArgumentException("Bad defense army size!");
		
		double oup = 0;
		
		for(int defLoss=0;defLoss<=2;defLoss++)
		{
			for(int atkLoss=0;atkLoss<=2;atkLoss++)
			{
				if(getOdds(atkDice,defDice,atkLoss,defLoss)!= 0)
					oup += getOdds(atkDice,defDice,atkLoss,defLoss)*getExpectedWinValue(atkSize-atkLoss,defSize-defLoss);
			}
		}
		expectedWinValueCache.put(new IntPair(atkSize,defSize), new Double(oup));
		return oup;
		
		
	}
	
	public static void main( String[] args )
	{
		RiskOdds r = new RiskOdds();
		int atkDice,defDice,atkL,defL;
		
		if(args.length > 0)
		{
			if(args.length < 2)
				throw new IllegalArgumentException("Need more command line arguments!");
			int atkSize = Integer.parseInt(args[0]);
			int defSize = Integer.parseInt(args[1]);
			System.out.println(" "+atkSize+" attacking "+defSize);
			System.out.println("\tOdds of Winning: "+r.getWinOdds(atkSize,defSize));
			System.out.println("\tExpected  Value: "+r.getExpectedWinValue(atkSize, defSize));
			
			System.exit(0);
		}
		
		for(defDice=1;defDice <= 2;defDice++)
		{
			for(atkDice=1;atkDice <= 3;atkDice++)
			{
				int[][] odds = calculateOdds(atkDice,defDice);
				int total = getTotalOutcomes(atkDice,defDice);
				double aExpectedValue=0;
				double dExpectedValue=0;
				ArrayList soup = new ArrayList();

				for(atkL=0;atkL < 3;atkL++)
				{
					for(defL=0;defL<3;defL++)
					{
						int numerator = odds[atkL][defL];
						int denominator = total;
						
						double percent = numerator;
						percent /= denominator;

						aExpectedValue+=(-atkL*percent);
						dExpectedValue+=(-defL*percent);
						
						percent*=10000;
						percent=Math.rint(percent);
						percent/=100;
						
						int fgcd = gcd(numerator,denominator);
						numerator/=fgcd;
						denominator/=fgcd;
						
						if(numerator != 0)
						{
							soup.add("\t ("+atkL+","+defL+"): "+((percent<10)?"0":"")+percent+"%"+" "+numerator+"/"+denominator);
						}
					}
				}
				aExpectedValue*=10000;
				aExpectedValue=Math.rint(aExpectedValue);
				aExpectedValue/=10000;
				
				dExpectedValue*=10000;
				dExpectedValue=Math.rint(dExpectedValue);
				dExpectedValue/=10000;
				
				System.out.println(" "+atkDice+" attacking "+((atkDice ==1)?"die ":"dice")+" vs. "+defDice+" defending "+((defDice ==1)?"die ":"dice")+":   ("+aExpectedValue+", "+dExpectedValue+")");
				java.util.Iterator i=soup.iterator();
				while(i.hasNext())
				{
					String cur = (String)i.next();
					System.out.println(cur);
				}
			}
		}		
	}

	
	public RiskOdds()
	{
		int atkDice,defDice,atkLoss,defLoss;
		if(amInitialized)
			return;
		totalProb = new double[4][4][4][4];
		defenseExpectedValue= new double[4][4];
		attackExpectedValue= new double[4][4];
		
				
		for(atkDice=0;atkDice<4;atkDice++)
		{
			for(defDice=0;defDice<4;defDice++)
			{
				attackExpectedValue[atkDice][defDice]=0;
				defenseExpectedValue[atkDice][defDice]=0;
				for(atkLoss=0;atkLoss<4;atkLoss++)
				{
					for(defLoss=0;defLoss<4;defLoss++)
					{
						totalProb[atkDice][defDice][atkLoss][defLoss]=0;
					}
				}
			}
		}
		
		for(defDice=1;defDice <= 2;defDice++)
		{
			for(atkDice=1;atkDice <= 3;atkDice++)
			{
				int[][] odds = calculateOdds(atkDice,defDice);
				int total = getTotalOutcomes(atkDice,defDice);
				double aExpectedValue=0;
				double dExpectedValue=0;

				for(atkLoss=0;atkLoss < 3;atkLoss++)
				{
					for(defLoss=0;defLoss<3;defLoss++)
					{
						int numerator = odds[atkLoss][defLoss];
						int denominator = total;
						
						double percent = numerator;
						percent /= denominator;

						aExpectedValue+=(-atkLoss*percent);
						dExpectedValue+=(-defLoss*percent);
						
						totalProb[atkDice][defDice][atkLoss][defLoss]=percent;
					}
				}
				attackExpectedValue[atkDice][defDice]=aExpectedValue;
				defenseExpectedValue[atkDice][defDice]=dExpectedValue;
			}
		}		
		amInitialized=true;
	}
	
	//think denominator
	public static int getTotalOutcomes(int atkNum,int defNum)
	{
		int oup=1;
		for(int x=0;x<atkNum+defNum;x++)
		{
			oup*=6;
		}
		return oup;
	}
	
	//think numerator of loss matrix
	public static int[][] calculateOdds(int atkNum, int defNum)
	{
		int[] attackingDice = new int[atkNum];
		int[] defendingDice = new int[defNum];
		
		for(int a = 0;a < atkNum;a++)
		{
			attackingDice[a]=0;
		}
		for(int d=0;d<defNum;d++)
		{
			defendingDice[d]=0;
		}
		
		int[][] lossMatrix = new int[3][3];
		for(int x=0;x<lossMatrix.length;x++)
		{
			for(int y=0;y<lossMatrix.length;y++)
			{
				lossMatrix[x][y]=0;
			}
		}
		
		while(incrementDice(attackingDice,defendingDice))
		{
			int curDefLost = getDefendingLoss(attackingDice,defendingDice);
			int curAtkLost = getAttackingLoss(attackingDice,defendingDice);
			lossMatrix[curAtkLost][curDefLost]++;
			if(DEBUG_PERMUTATION)
			{
				for(int x=0;x<attackingDice.length;x++)
				{
					System.out.print(attackingDice[x]+",");
				}
				for(int x=0;x<defendingDice.length;x++)
				{
					System.out.print(defendingDice[x]+",");
				}
				System.out.println();
			}
		}
		return lossMatrix;
	}
	
	public static boolean incrementDice(int[] attackingDice,int[] defendingDice)
	{
		if(attackingDice.length <= 0 || defendingDice.length<=0)
			throw new IllegalArgumentException("Zero Length Array!");
		
		if(attackingDice[0]==0)
		{
			for(int x=0;x<attackingDice.length;x++)
			{
				attackingDice[x]=1;
			}
			for(int x=0;x<defendingDice.length;x++)
			{
				defendingDice[x]=1;
			}
			return true;
		}
		
		int cd = defendingDice.length-1;
		while(cd >= 0 && defendingDice[cd] == 6)
		{
			cd--;
		}
		
		if(cd >= 0)
		{
			defendingDice[cd]++;
			for(int x=cd+1;x<defendingDice.length;x++)
			{
				defendingDice[x]=1;
			}
			return true;
		}
		
		cd = attackingDice.length-1;
		while(cd >= 0 && attackingDice[cd] == 6)
		{
			cd--;
		}
		
		if(cd >= 0)
		{
			attackingDice[cd]++;
			for(int x=cd+1;x<attackingDice.length;x++)
			{
				attackingDice[x]=1;
			}
			for(int x=0;x<defendingDice.length;x++)
			{
				defendingDice[x]=1;
			}
			return true;
		}
		
		return false;
	}
	
	public static int getDefendingLoss(int[] attackingDice,int[] defendingDice)
	{
		if(attackingDice.length <= 0 || defendingDice.length <= 0)
			throw new IllegalArgumentException("Zero length array!");
		
		int[] sAtk = sortedCloneArray(attackingDice);
		int[] sDef = sortedCloneArray(defendingDice);
		int minLength = Math.min(sAtk.length,sDef.length);
		
		int defLoss=0;
		if(sAtk[0] > sDef[0])
			defLoss++;
		if(minLength <= 1)
			return defLoss;
		if(sAtk[1] > sDef[1])
			defLoss++;
		return defLoss;		
	}
	
	public static int getAttackingLoss(int[] attackingDice,int[] defendingDice)
	{
		if(attackingDice.length <= 0 || defendingDice.length <= 0)
			throw new IllegalArgumentException("Zero length array!");
		
		int[] sAtk = sortedCloneArray(attackingDice);
		int[] sDef = sortedCloneArray(defendingDice);
		int minLength = Math.min(sAtk.length,sDef.length);
		
		int atkLoss=0;
		if(sAtk[0] <= sDef[0])
			atkLoss++;
		if(minLength <= 1)
			return atkLoss;
		if(sAtk[1] <= sDef[1])
			atkLoss++;
		return atkLoss;		
	}
	
	private static int[] sortedCloneArray(int[] arr)
	{
		int oup[] = new int[arr.length];
		
		for(int x=0;x<arr.length;x++)
		{
			oup[x] = arr[x];
		}
		
		if(oup.length <= 1)
			return oup;
		
		boolean cont = true;
		while(cont)
		{
			cont=false;
			for(int x=1;x<oup.length;x++)
			{
				if(oup[x-1] < oup[x])
				{
					int temp = oup[x-1];
					oup[x-1]=oup[x];
					oup[x]=temp;
					cont=true;
				}
			}
		}
		
		return oup;		
	}
	
	public static int gcd(int x,int y)
	{
        if (x == 0)
        	return y;
        if (y == 0)
        	return x;
        int r2 = x;
        int r1 = y;
        int g,r;
        while (true)
        {
                g = r2 / r1;
                r = r2 - r1 * g;
                if (r == 0)
                	return r1;
                r2 = r1;
                r1 = r;
        }
	}
}
