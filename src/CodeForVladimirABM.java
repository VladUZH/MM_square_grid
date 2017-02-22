import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Random;


public class AgentModelDetail extends AtomAbstr implements Atom {
	private static final long serialVersionUID = 1L;
	
	/* -- INPUTS -- */
	public AtomInput feed;
	public AtomInput trigger;
	
	/* -- CONFIGURATION -- */
	AgentModelDetailAtomConfiguration cfg;
	
	
	public class Runner{
		public double prevExtreme;
		public long prevExtremeTime;
		
		public double prevDC;
		public long prevDCTime;
		
		public double extreme;
		public long extremeTime;
		
		public double delta;
		public double dStar;
		public double osL;
		public int type;
		public boolean initalized;
		public double reference;
		
		public String fileName;
		
		public Runner(double thresh, PriceFeedData price, String file, double deltaStar){
			prevExtreme = price.elems.mid; prevExtremeTime = price.elems.time;
			prevDC = price.elems.mid; prevDCTime = price.elems.time;
			extreme = price.elems.mid; extremeTime = price.elems.time;
			reference = price.elems.mid; dStar = deltaStar;
			
			type = -1; delta = thresh; osL = 0.0; initalized = true;
			fileName = new String(file);
		}
		public Runner(double thresh, double price, String file, double deltaStar){
			prevExtreme = price; prevExtremeTime = 0;
			prevDC = price; prevDCTime = 0;
			extreme = price; extremeTime = 0;
			reference = price; dStar = deltaStar;
			
			type = -1; delta = thresh; osL = 0.0; initalized = true;
			fileName = new String(file);
		}
		
		public Runner(double thresh, String file, double deltaStar){
			delta = thresh;
			initalized = false;
			fileName = new String(file);
			dStar = deltaStar;
		}
		
		public int run(PriceFeedData price){
			if( price == null )
				return -1;
			if( !initalized ){
				type = -1; osL = 0.0; initalized = true;
				prevExtreme = price.elems.mid; prevExtremeTime = price.elems.time;
				prevDC = price.elems.mid; prevDCTime = price.elems.time;
				extreme = price.elems.mid; extremeTime = price.elems.time;
				reference = price.elems.mid;
				
				return -1;
			}
			
			if( type == -1 ){
				if( Math.log(price.elems.mid/extreme) >= delta ){
					prevExtreme = extreme;
					prevExtremeTime = extremeTime;
					type = 1;
					extreme = price.elems.mid; extremeTime = price.elems.time;
					prevDC = price.elems.mid; prevDCTime = price.elems.time;
					reference = price.elems.mid;
					return 0;
				}
				if( price.elems.mid < extreme ){
					extreme = price.elems.mid;
					extremeTime = price.elems.time;
					osL = -Math.log(extreme/prevDC)/delta;
					return 1;
				}
			}else if( type == 1 ){
				if( Math.log(price.elems.mid/extreme) <= -delta ){
					prevExtreme = extreme; 
					prevExtremeTime = extremeTime;
					type = -1;
					extreme = price.elems.mid; extremeTime = price.elems.time;
					prevDC = price.elems.mid; prevDCTime = price.elems.time;
					reference = price.elems.mid;
					return 0;
				}
				if( price.elems.mid > extreme ){
					extreme = price.elems.mid; 
					extremeTime = price.elems.time;
					osL = Math.log(extreme/prevDC)/delta;
					return 1;
				}
			}
			return -1;
		}
		public int run(double price){
			if( !initalized ){
				type = -1; osL = 0.0; initalized = true;
				prevExtreme = price; prevExtremeTime = 0;
				prevDC = price; prevDCTime = 0;
				extreme = price; extremeTime = 0;
				reference = price;
				return 0;
			}
			
			if( type == -1 ){
				if( price - extreme >= delta ){
					prevExtreme = extreme;
					prevExtremeTime = extremeTime;
					type = 1;
					extreme = price; extremeTime = 0;
					prevDC = price; prevDCTime = 0;
					reference = price;
					osL = 0.0;
					return 1;
				}
				if( price < extreme ){
					extreme = price;
					extremeTime = 0;
					osL = -(extreme - prevDC);
					if( reference - extreme >= dStar ){
						reference = extreme;
						return 2;
					}
					return 0;
				}
			}else if( type == 1 ){
				if( price - extreme <= -delta ){
					prevExtreme = extreme; prevExtremeTime = extremeTime;
					type = -1;
					extreme = price; extremeTime = 0;
					prevDC = price; prevDCTime = 0;
					reference = price;
					osL = 0.0;
					return 1;
				}
				if( price > extreme ){
					extreme = price; extremeTime = 0;
					osL = (extreme -prevDC);
					if( extreme - reference >= dStar ){
						reference = extreme;
						return 2;
					}
					return 0;
				}
			}
			return 0;
		}
	}
	
	public class Price{
		double bid;
		double ask;
		Price(){};
		Price(Price p){
			bid = p.bid;
			ask = p.ask;
		}
		Price(double b, double a){
			bid = b;
			ask = a;
		}
		Price(PriceFeedData price){
			bid = price.elems.bid;
			ask = bid + 0.00001;
		}
	}
	
	public class Surprise{
		long time;
		double surprise;
		Surprise(){};
		Surprise(long t, double s){
			time = t;
			surprise = s;
		}
	};
	
	public class Go{
		public Runner[] runner;
		double[] prevState;
		double[] surprises; double[] downSurp; double[] upSurp;
		double[] surprisesT; double[] downSurpT; double[] upSurpT;
		double liquidity; 
		double liqEMA;
		double upLiq, downLiq, diffLiq;
		double H1 = 0.0, H2 = 0.0;
		
		public Go(){};
		public Go(PriceFeedData price, double delta1, double delta2, int lgt){			
			H1 = 1.0;
			H2 = 1.978111911 - H1*H1;
			
			runner = new Runner[lgt];
			prevState = new double[lgt];
			
			surprises = new double[lgt]; surprisesT = new double[lgt]; 
			downSurp = new double[lgt]; downSurpT = new double[lgt];
			upSurp = new double[lgt]; upSurpT = new double[lgt];
			
			for( int i = 0; i < runner.length; ++i ){
				runner[i] = new Runner(delta1*(double)(i+1), price, "JustFake", 2.48530673*delta1*(double)(i+1));
				runner[i].type = (i%2 == 0 ? 1 : -1);
				prevState[i] = (runner[i].type == 1 ? 1 : 0);
				surprises[i] = H1; surprisesT[i] = H1;
				downSurp[i] = H1; downSurpT[i] = H1;
				upSurp[i] = H1; upSurpT[i] = H1;
			}
			liquidity = 0.0; 
			liqEMA = 0.0;
			
			downLiq = 0.0; 
			upLiq = 0.0; 
			diffLiq = 0.0; 
		}
		public boolean Trigger(PriceFeedData price){
			/* -- update values -- */
			boolean doComp = false;
			for( int i = 0; i < runner.length; ++i ){
				int value = runner[i].run(price);
				if( value >= 0 ){
					double alpha = 2.0/(50.0 + 1.0);
					if( value == 1 ){
						double myProbs = Math.exp(-runner[i].osL);
						surprisesT[i] = surprises[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						if( runner[i].type == -1 ){
							downSurpT[i] = downSurp[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						}else if( runner[i].type == 1 ){
							upSurpT[i] = upSurp[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						}
					}else if( value == 0 ){
						double myProbs = Math.exp(-runner[i].osL);
						surprises[i] = surprises[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						if( runner[i].type == -1 ){
							upSurp[i] = upSurp[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						}else if( runner[i].type == 1 ){
							downSurp[i] = downSurp[i]*Math.exp(-alpha) + (1.0 - Math.exp(-alpha))*(-Math.log(myProbs));
						}
						runner[i].osL = 0.0;
					}else{
						//should never happen
					}
					doComp = true;
				}
			}
			
			if( doComp ){
				double liqEMAT = 0.0; double upLiqT = 0.0; double downLiqT = 0.0; double diffLiqT = 0.0;  
				for( int i = 0; i < runner.length; ++i ){
					liqEMAT += Math.sqrt(50.0)*(surprisesT[i] - H1)/Math.sqrt(H2);
					upLiqT += Math.sqrt(50.0)*(upSurpT[i] - H1)/Math.sqrt(H2);
					downLiqT += Math.sqrt(50.0)*(downSurpT[i] - H1)/Math.sqrt(H2);
					diffLiqT += Math.sqrt(50.0)*(upSurpT[i] - downSurpT[i])/Math.sqrt(H2);
				}
				liqEMA = (1.0 - CumNorm(liqEMAT/Math.sqrt(runner.length)));
				upLiq = (1.0 - CumNorm(upLiqT/Math.sqrt(runner.length)));
				downLiq =  (1.0 - CumNorm(downLiqT/Math.sqrt(runner.length)));
				diffLiq = CumNorm(diffLiqT/Math.sqrt(runner.length));
			}
			return doComp;
		}
		
		public double getProbs(int i){
			int where = -1;
			for( int j = 1; j < prevState.length; ++j ){
				if( prevState[j] != prevState[0] ){
					where = j;
					break;
				}
			}
			if( i > 0 && where != i ){
				System.out.println("This should not happenn!");
			}
			prevState[i] = (prevState[i] == 1 ? 0 : 1);
			
			if( where == 1 ){
				if( i > 0 ){
					return Math.exp(-(runner[1].delta - runner[0].delta)/runner[0].delta);
				}else{
					return (1.0 - Math.exp(-(runner[1].delta - runner[0].delta)/runner[0].delta));
				}
			}else if( where > 1 ){
				double numerator = 0.0;
				for( int k = 1; k <= where; ++k ){
					numerator -= (runner[k].delta - runner[k-1].delta)/runner[k-1].delta;
				}
				numerator = Math.exp(numerator);
				double denominator = 0.0;
				for( int k = 1; k <= where - 1; ++k ){
					double secVal = 0.0;
					for( int j  = k+1; j <= where; ++j ){
						secVal -=  (runner[j].delta - runner[j-1].delta)/runner[j-1].delta;
					}
					denominator += (1.0 - Math.exp(-(runner[k].delta - runner[k-1].delta)/runner[k-1].delta))*Math.exp(secVal);
				}
				if( i > 0 ){
					return numerator/(1.0 - denominator);
				}else{
					return (1.0 - numerator/(1.0 - denominator));
				}
			}else{
				return 1.0;
			}
		}
		// another implementation of the CNDF
		// for a standard normal: N(0,1)
		double CumNorm(double x)
		{
			// protect against overflow
			if (x > 6.0)
				return 1.0;
			if (x < -6.0)
				return 0.0;
		 
			double b1 = 0.31938153;
			double b2 = -0.356563782;
			double b3 = 1.781477937;
			double b4 = -1.821255978;
			double b5 = 1.330274429;
			double p = 0.2316419;
			double c2 = 0.3989423;
		 
			double a = Math.abs(x);
			double t = 1.0 / (1.0 + a * p);
			double b = c2*Math.exp((-x)*(x/2.0));
			double n = ((((b5*t+b4)*t+b3)*t+b2)*t+b1)*t;
			n = 1.0-b*n;
			
			if ( x < 0.0 )
				n = 1.0 - n;

			return n;
		}
		
		/*
		public double computeLiquidity(long deltaT){
			double surpT = 0.0;
			if( mySurprises.size() <= 200.0 ){
				return 0.0;
			}else{
				mySurprises.remove(0);
			}
			
			double upN = 0.0, downN = 0.0;
			double downSurprise = 0.0, upSurprise = 0.0;
			for( int i = 0; i < mySurprises.size(); ++i ){
				double sTemp = mySurprises.get(i).surprise;
				surpT += -Math.log(Math.abs(sTemp));
				if( sTemp < 0.0 ){
					downSurprise += -Math.log(-sTemp);
					downN += 1.0;
				}else if( sTemp > 0.0 ){
					upSurprise += -Math.log(sTemp);
					upN += 1.0;
				}else{
					System.out.println("Not sure if this can happen!");
				}
			}
			downSurprise /= downN;
			upSurprise /= upN;
			return (1.0 - CumNorm((surpT - H1*mySurprises.size())/Math.sqrt(H2*mySurprises.size())));
		}
		*/
	};
	
	
	public class HighOS{
		public double prevExtreme;
		public long prevExtremeTime;
		
		public double prevDC;
		public long prevDCTime;
		
		public double extreme;
		public long extremeTime;
		
		public double threshold;
		public double osL;
		public int type;
		public boolean initalized;
		
		public int counter;
		
		public String fileName;
		
		public double omegaSum;
		public double referencePrice;
		
		public HighOS(double thresh, PriceFeedData price, String file){
			prevExtreme = price.elems.mid; prevExtremeTime = price.elems.time;
			prevDC = price.elems.mid; prevDCTime = price.elems.time;
			extreme = price.elems.mid; extremeTime = price.elems.time;  
			
			type = -1; threshold = thresh; osL = 0.0; initalized = true;
			fileName = new String(file);
			
			counter = 0;
			
			omegaSum = 0.0;
			referencePrice = price.elems.mid;
		}
		
		public HighOS(double thresh, String file){
			threshold = thresh;
			initalized = false;
			fileName = new String(file);
			counter = 0;
		}
		public boolean run(PriceFeedData price){
			if( price == null )
				return false;
			if( !initalized ){
				type = -1; osL = 0.0; initalized = true;
				prevExtreme = price.elems.mid; prevExtremeTime = price.elems.time;
				prevDC = price.elems.mid; prevDCTime = price.elems.time;
				extreme = price.elems.mid; extremeTime = price.elems.time;
				counter = 0;
				
				referencePrice = price.elems.mid;
				return false;
			}
			
			if( type == 1 ){
				if( Math.log(price.elems.mid/extreme) >= threshold ){
					prevExtreme = extreme;
					prevExtremeTime = extremeTime;
					type = -1;
					extreme = price.elems.mid; extremeTime = price.elems.time;
					prevDC = price.elems.mid; prevDCTime = price.elems.time;
					osL = 0.0;
					counter++;
					
					referencePrice = price.elems.mid;
					return true;
				}
				if( price.elems.mid < extreme ){
					extreme = price.elems.mid;
					extremeTime = price.elems.time;
					osL = -Math.log(extreme/prevDC);
					
					if( Math.log(referencePrice/price.elems.mid) >= threshold ){
						referencePrice = price.elems.mid;
						return true;
					}
					return false;
				}
			}else if( type == -1 ){
				if( Math.log(price.elems.mid/extreme) <= -threshold ){
					prevExtreme = extreme; prevExtremeTime = extremeTime;
					type = 1;
					extreme = price.elems.mid; extremeTime = price.elems.time;
					prevDC = price.elems.mid; prevDCTime = price.elems.time;
					osL = 0.0;
					counter++;
					
					referencePrice = price.elems.mid;
					return true;
				}
				if( price.elems.mid > extreme ){
					extreme = price.elems.mid; extremeTime = price.elems.time;
					osL = Math.log(extreme/prevDC);
					
					if( Math.log(referencePrice/price.elems.mid) <= -threshold ){
						referencePrice = price.elems.mid;
						return true;
					}
					return false;
				}
			}
			return false;
		}
		public boolean run(double price){
			if( !initalized ){
				type = -1; osL = 0.0; initalized = true;
				prevExtreme = price; 
				prevDC = price; 
				extreme = price;
				counter = 0;
				
				omegaSum = 0.0;
				referencePrice = price;
				return false;
			}
			
			if( type == 1 ){
				if( (price - extreme) >= threshold ){
					prevExtreme = extreme;
					prevExtremeTime = extremeTime;
					type = -1;
					extreme = price;
					prevDC = price;
					osL = 0.0;
					counter++;
					
					referencePrice = price;
					return true;
				}
				if( price < extreme ){
					extreme = price;
					osL = -(extreme-prevDC);
					if( referencePrice - price >= threshold ){
						referencePrice = price;
						return true;
					}
					return false;
				}
			}else if( type == -1 ){
				if( (price - extreme) <= -threshold ){
					prevExtreme = extreme; prevExtremeTime = extremeTime;
					type = 1;
					extreme = price;
					prevDC = price;
					osL = 0.0;
					counter++;
					referencePrice = price;
					return true;
				}
				if( price > extreme ){
					extreme = price;
					osL = (extreme-prevDC);
					
					if( referencePrice - price <= -threshold ){
						referencePrice = price;
						return true;
					}
					return false;
				}
			}
			return false;
		}
	};
	
	public class Running{
		public HighOS[] highOS_1;
		double prev1;
		
		public String ccyName;
		public int positionSide;
		
		double allDelta, pnl, tradePrice;
		double oppositePnl, oppositeTradePrice;
		boolean initalized;
		boolean[] whichTicked;
		
		public Running(double thresholds, String ccy, double randomHelp){ 
			highOS_1 = new HighOS[1];
			ccyName = new String(ccy);
			
			allDelta = 0.0; initalized = false; pnl = 0.0;
			
			highOS_1[0] = new HighOS(thresholds, new String(0 + ".dat"));
			//highOS_1[1] = new HighOS(2.0*thresholds, new String(1 + ".dat"));
			
			positionSide = (randomHelp < 0.5 ? -1 : 1);
			whichTicked = new boolean[1];
			
			/*
			 * 
			 * 			
			double startThreshold = cfg.startThreshold.value().doubleValue();
			double endThreshold = cfg.endThreshold.value().doubleValue();
			
			
			switch( cfg.type.value() ){
			case LOG : 
				double space1 = (Math.log(endThreshold)-Math.log(startThreshold))/(numberOfDc-1.0);
				for( int i = 0; i < numberOfDc; ++i ){
					highOS[i] = new HighOS(startThreshold*Math.exp(((double)i)*space1), new String(tempFileName + i + ".dat"));
					try{
						fw.append(startThreshold*Math.exp(((double)i)*space1)+"\n");
					}catch(IOException e){
						System.out.println("Failed printing dc thres! "+e.getMessage());
					}
				}
				break;
			case LINEAR : 
				double space2 = (endThreshold-startThreshold)/(numberOfDc-1.0);
				for( int i = 0; i < numberOfDc; ++i ){
					highOS[i] = new HighOS(startThreshold+((double)i)*space2, new String(tempFileName + i + ".dat"));
					try{
						fw.append(startThreshold+((double)i)*space2+"\n");
					}catch(IOException e){
						System.out.println("Failed printing dc thres! "+e.getMessage());
					}
				}
				break;
			case BYPASS : 
				double[] thresholdsT = {0.1/100.0, 0.05/100.0, 0.025/100.0, 0.0125/100.0 };
				for( int i = 0; i < thresholdsT.length; ++i ){
					highOS[i] = new HighOS(thresholdsT[i], new String(tempFileName + i + ".dat"));
					try{
						fw.append(thresholdsT[i] + "\n");
					}catch(IOException e){
						System.out.println("Failed at printing dc thres! " + e.getMessage());
					}
				}
			} 
			try{
				fw.close();
			}catch(Exception e){
				System.out.println("Failed closing thresholds file!");
			}*/
			
		}
		
		public boolean update(PriceFeedData p1){
			boolean returnToPrint = false;
			
			if( !initalized ){
				tradePrice = (positionSide == 1 ? p1.elems.mid : p1.elems.mid);
				oppositeTradePrice = (positionSide == 1 ? p1.elems.mid : p1.elems.mid);
				initalized = true;
			}
			for( int i = 0; i < highOS_1.length; ++i ){
				if( highOS_1[i].run(p1) ){
					whichTicked[i] = true;
					returnToPrint = true;
				}
			}
			return returnToPrint;
		}
		public boolean trySwitch(double toSwitch, PriceFeedData price){
			double prob = 1.0;
			boolean returnToPrint = false;
			for( int i = 0; i < highOS_1.length; ++i ){
				if( whichTicked[i] ){
					prob *= (highOS_1[i].type != positionSide ? (1.0 - Math.exp(-1.0)) : Math.exp(-1.0)); 
					
					if( toSwitch < prob ){
						pnl += (positionSide == 1 ? price.elems.mid - tradePrice : tradePrice - price.elems.mid)/tradePrice;
						oppositePnl += (positionSide == 1 ? oppositeTradePrice - price.elems.mid : price.elems.mid - oppositeTradePrice)/oppositeTradePrice;
						
						positionSide *= -1.0;
						
						tradePrice = (positionSide == 1 ? price.elems.mid : price.elems.mid);
						oppositeTradePrice = (positionSide == 1 ? price.elems.mid : price.elems.mid);
						for( int j = 0; j < whichTicked.length; ++j ){
							whichTicked[j] = false;
						}
						return true;
					}
				}
			}
			return returnToPrint;
		}
		public double getRealTimePNL(PriceFeedData price){
			double tPNL = (this.positionSide == 1 ? price.elems.mid - tradePrice : tradePrice - price.elems.mid)/tradePrice;
			return tPNL;
		}
		public double getRealTimeOpPNL(PriceFeedData price){
			double tPNL = (this.positionSide == 1 ? tradePrice - price.elems.mid : price.elems.mid - tradePrice)/tradePrice;
			return tPNL;
		}
	};
	
	public class Agents{
		public Running[] runArray;
		public int[] vecInt;
		public String ccyRate;
		Random rand;
		public double Price;
		public boolean init;
		long startTime;
		Go go;
		
		public class HelpClass{
			long time;
			double price;
			int totalLong;
			int longPos;
			int longNeg;
			int shortPos;
			int shortNeg;
			double totalPNL;
			double totalOpPNL;
			double upLiq; double downLiq; double liq;
			List<Double> pnls; List<Integer> longs;
			HelpClass(){};
			HelpClass(long t, double p, int tL, int lP, int lN, int sP, int sN, double tP, double tOP, double uL, double dL, double L, double[] P, int[] Longs){
				time = t; price = p; totalLong = tL; longPos = lP; longNeg = lN; shortPos = sP; shortNeg = sN; totalPNL = tP; totalOpPNL = tOP; 
				upLiq = uL; downLiq = dL; liq = L;
				pnls = new LinkedList<Double>(); longs = new LinkedList<Integer>();
				for( int i = 0; i < P.length; ++i ){
					pnls.add(new Double(P[i]));
					longs.add(new Integer(Longs[i]));
				}
			}
		};
		List<HelpClass> helpC;
		
		public Agents(){};
		public Agents(String ccyName){
			
			runArray = new Running[cfg.numberOf.value().intValue()];
			vecInt = new int[cfg.numberOf.value().intValue()];
			ccyRate = new String(ccyName);
			rand = new Random(1);
			startTime = 1136073600;
			helpC = new LinkedList<HelpClass>();
			
			double[] thresholds = new double[cfg.numberOf.value().intValue()];
			double space2 = (cfg.endThreshold.value().doubleValue() - cfg.startThreshold.value().doubleValue())/(cfg.numberOf.value().doubleValue() - 1.0);
			
			for( int i = 0; i < thresholds.length; ++i ){
				thresholds[i] = cfg.startThreshold.value().doubleValue() + ((double)i)*space2;
			}
			 
			for( int j = 0; j < runArray.length; ++j ){
				double nextRand = rand.nextDouble();
				runArray[j] = new Running(thresholds[j], ccyRate, nextRand);
				vecInt[j] = (nextRand < 0.5 ? 1 : 0);
			}
		}
		
		public boolean update(PriceFeedData price){
			if( init ){
				if( price.elems.mid == Price ){
					return true;
				}else{
					Price = price.elems.mid;
				}
			}
			if( !init ){
				init = true;
				go = new Go(price, 0.025/100.0, 0.025/100.0, 20);
				Price = price.elems.mid;
			}
			boolean[] trueArray = new boolean[runArray.length];
			boolean goForPrint = false;
			go.Trigger(price);
			
			for( int i = 0; i < runArray.length; ++i ){
				if( runArray[i].update(price) ){
					trueArray[i] = true; goForPrint = true;
				}
			}
			
			boolean reallyPrint = false;
			if( goForPrint ){							
				for( int i = 0; i < runArray.length; ++i ){
					if( trueArray[i] && runArray[i].trySwitch(this.rand.nextDouble(), price) ){
						reallyPrint = true;
					}
				}
				int totalLong = 0; double totalPNL = 0.0; double totalOpPNL = 0.0;
				int LongPos = 0; int LongNeg = 0; int ShortPos = 0; int ShortNeg = 0;
				double[] totalPNLs = new double[5]; int[] totalLongS = new int[5];
				for( int l = 0; l < runArray.length; ++l ){
					totalLong += (runArray[l].positionSide == 1 ? 1 : 0);
				}
				
				for( int l = 0; l < runArray.length; ++l ){
					totalLong += (runArray[l].positionSide == 1 ? 1 : 0);
					LongPos += (runArray[l].positionSide == 1 && runArray[l].getRealTimePNL(price) >= 0.0 ? 1 : 0);
					LongNeg += (runArray[l].positionSide == 1 && runArray[l].getRealTimePNL(price) <  0.0 ? 1 : 0);
					
					ShortPos += (runArray[l].positionSide != 1 && runArray[l].getRealTimePNL(price) >= 0.0 ? 1 : 0);
					ShortNeg += (runArray[l].positionSide != 1 && runArray[l].getRealTimePNL(price) <  0.0 ? 1 : 0);
					
					totalPNL += runArray[l].pnl + runArray[l].getRealTimePNL(price);
					
					totalPNLs[0] += (0   <= l && l < 100 ? (runArray[l].pnl + runArray[l].getRealTimePNL(price)) : 0.0);
					totalPNLs[1] += (100 <= l && l < 200 ? (runArray[l].pnl + runArray[l].getRealTimePNL(price)) : 0.0);
					totalPNLs[2] += (200 <= l && l < 300 ? (runArray[l].pnl + runArray[l].getRealTimePNL(price)) : 0.0);
					totalPNLs[3] += (300 <= l && l < 400 ? (runArray[l].pnl + runArray[l].getRealTimePNL(price)) : 0.0);
					totalPNLs[4] += (400 <= l && l < 500 ? (runArray[l].pnl + runArray[l].getRealTimePNL(price)) : 0.0);
					
					totalLongS[0] += (0   <= l && l < 100 && runArray[l].positionSide == 1 ? 1 : 0);
					totalLongS[1] += (100 <= l && l < 200 && runArray[l].positionSide == 1 ? 1 : 0);
					totalLongS[2] += (200 <= l && l < 300 && runArray[l].positionSide == 1 ? 1 : 0);
					totalLongS[3] += (300 <= l && l < 400 && runArray[l].positionSide == 1 ? 1 : 0);
					totalLongS[4] += (400 <= l && l < 500 && runArray[l].positionSide == 1 ? 1 : 0);
				}
				helpC.add(new HelpClass(price.elems.time, price.elems.mid, totalLong, LongPos, LongNeg, ShortPos, ShortNeg, totalPNL, totalOpPNL, 
						go.upLiq, go.downLiq, go.liqEMA, totalPNLs, totalLongS));
			}
			
			if( helpC.size() > 100000 ){
				// immediately move clocker
				
				//Print here the number of long/short traders
				FileWriter fw = null;
				String sep = new String(System.getProperty("file.separator"));
				String folder = new String(sep + "home" + sep + "agolub" + sep + "workspace" + sep + "AgentModelMulti" + sep);
				try{
					fw = new FileWriter(new String(folder + this.ccyRate + "LongShortNew.dat"), true);
					for( int i = 0; i < helpC.size(); ++i ){
						fw.append(helpC.get(i).time + "," + helpC.get(i).price + "," + helpC.get(i).totalLong + "," + helpC.get(i).longPos + "," + helpC.get(i).longNeg + "," 
								+ helpC.get(i).shortPos + "," + helpC.get(i).shortNeg + "," + helpC.get(i).totalPNL + "," + helpC.get(i).totalOpPNL);
						for( int j = 0; j < helpC.get(i).pnls.size(); ++j ){
							fw.append("," + helpC.get(i).pnls.get(j).doubleValue());
						}
						for( int j = 0; j < helpC.get(i).longs.size(); ++j ){
							fw.append("," + helpC.get(i).longs.get(j).intValue());
						}
						fw.append("\n");
					}
					fw.close();
					helpC.clear();
					/*
					fw = new FileWriter(new String(folder + this.ccyRate + "EntryPosNew.dat"), true);
					fw.append(price.elems.time + "," + price.elems.mid);
					for( int l = 0; l < runArray.length-1; ++l ){
						fw.append("," + (runArray[l].positionSide == 1 ? 1.0 : -1.0)*runArray[l].tradePrice);
					}
					fw.append("," + (runArray[runArray.length-1].positionSide == 1 ? 1.0 : -1.0)*runArray[runArray.length-1].tradePrice + "\n");
					fw.close();
					*/
					
				}catch(IOException e){
					getLogger().error("Failed opening printing values " + e.getMessage());
					return false;
				}
			}
			
			return true;
		}
	};
	
	AgentModelDetailStatus st = new AgentModelDetailStatus();
	public static class AgentModelDetailStatus implements Serializable {
		private static final long serialVersionUID = 1L;

		public Agents[] array = null;
		public boolean initilized = false;
	}
	
	
	public AgentModelDetail(RoutesFramework framework){
		super(framework, "Engine");
		
		cfg = new AgentModelDetailAtomConfiguration();
		
		/* -- handling the feed -- */
		//final DataType priceType = PriceFeedData.priceFeedType;
		feed = new AtomInputAbstr(this,"feed", "Input the price feed"){
			private static final long serialVersionUID = 1L;
			public DataType getType() {
				return ArrayDataElement.Factory.buildType(PriceFeedData.priceFeedType);
			}
			public RoutesStep[] receive_internal(DataElement data) throws AtomException {
				if( data == null){
					System.out.println("Null element for input!");
					return null;
				}
				if( st == null ){
					st = new AgentModelDetailStatus();
				}
				ArrayDataElement ar = data.toArrayDE();
				
				if( !st.initilized ){
					if( !initilize(ar.getAll()) ){
						throw new AtomException("Agent Model Detail initilizer failed!");
					}
					return null;
				}else{
					if( !handleScaling(ar.getAll()) ){
						/* -- updating Scaling failed, throw an exception -- */
						throw new AtomException("Agent Model Detail update failed!");
					}
					return null;
				}
				
			}
		};
		
		trigger = new AtomInputAbstr(this, "trigger", "End of Data trigger to print"){
			private static final long serialVersionUID = 1L;
			public DataType getType() throws AtomException {
				return NullDataElement.nullDataType;
			}
			public RoutesStep[] receive_internal(DataElement data)
					throws AtomException {
					if( !getPrintOut() ){
						throw new AtomException("Simulation failed!");
					}
					return null;
			}
		};	
	}
	
	public boolean initilize(DataElement data[]){
		String[] ccyList = {"AUD_CAD", "AUD_JPY", "AUD_NZD", "AUD_USD", "CAD_JPY", "CHF_JPY", "EUR_AUD", "EUR_CAD", "EUR_CHF",
				"EUR_GBP", "EUR_JPY", "EUR_NZD", "EUR_USD", "GBP_AUD", "GBP_CAD", "GBP_CHF", "GBP_JPY", "GBP_USD", "NZD_CAD",
				"NZD_JPY", "NZD_USD", "USD_CAD", "USD_CHF", "USD_JPY"}; 
		
		st.array = new Agents[ccyList.length];
		for( int i = 0; i < st.array.length; ++i ){
			st.array[i] = new Agents(ccyList[i]);
		}
		st.initilized = true;
		
		/* -- printing details set by user -- */
		FileWriter fw = null;
		String sep = new String(System.getProperty("file.separator"));
		String folder = new String(sep + "home" + sep + "agolub" + sep + "workspace" + sep + "AgentModelMulti" + sep);
		
		try{
			fw = new FileWriter(new String(folder + "DetailsNew.dat"), true);
			fw.append(cfg.startThreshold.value().doubleValue() + "\n" + cfg.endThreshold.value().doubleValue() + "\n" + 
					cfg.numberOf.value().intValue() + "\n" + cfg.overlapp.value().intValue() + "\n" + cfg.howFar.value().intValue() + "\n");
			fw.close();
		}catch(IOException e){
			getLogger().error("Failed opening printing values " + e.getMessage());
			return false;
		}
		/* ------------------------------- */
		
		for( DataElement price : data ){
			PriceFeedData p = PriceFeedData.cast(price.toAggregatedDE());
			for( int i = 0; i < st.array.length; ++i ){
				if( st.array[i].ccyRate.equals(p.elems.instrument.toString()) ){
					if( st.array[i].update(p) ){
						// print Out Here For Position Map
					}
					break;
				}
			}
		}

		return true;
	}
	
	public boolean handleScaling(DataElement data[]){
		for( DataElement price : data ){
			PriceFeedData p = PriceFeedData.cast(price.toAggregatedDE());
			for( int i = 0; i < st.array.length; ++i ){
				if( st.array[i].ccyRate.equals(p.elems.instrument.toString()) ){
					if( !st.array[i].update(p) ){
						System.out.println("Failed at updating!");
						return false;
					}
					break;
				}
			}
		}
		return true;
	}
	
	public boolean getPrintOut(){
		for( int j = 0; j < st.array.length; ++j ){
			FileWriter fw = null;
			String sep = new String(System.getProperty("file.separator"));
			String folder = new String(sep + "home" + sep + "agolub" + sep + "workspace" + sep + "AgentModelMulti" + sep);
			try{
				fw = new FileWriter(new String(folder + st.array[j].ccyRate + "LongShortNew.dat"), true);
				for( int i = 0; i < st.array[j].helpC.size(); ++i ){
					fw.append(st.array[j].helpC.get(i).time + "," + st.array[j].helpC.get(i).price + "," + st.array[j].helpC.get(i).totalLong + "," + st.array[j].helpC.get(i).longPos 
							+ "," + st.array[j].helpC.get(i).longNeg + "," + st.array[j].helpC.get(i).shortPos + "," + st.array[j].helpC.get(i).shortNeg + "," 
							+ st.array[j].helpC.get(i).totalPNL + "," + st.array[j].helpC.get(i).totalOpPNL);
					for( int k = 0; k < st.array[j].helpC.get(i).pnls.size(); ++k ){
						fw.append("," + st.array[j].helpC.get(i).pnls.get(k).doubleValue());
					}
					for( int k = 0; k < st.array[j].helpC.get(i).longs.size(); ++k ){
						fw.append("," + st.array[j].helpC.get(i).longs.get(j).intValue());
					}
					fw.append("\n");
				}
				fw.close();
				st.array[j].helpC.clear();
				/*
				fw = new FileWriter(new String(folder + this.ccyRate + "EntryPosNew.dat"), true);
				fw.append(price.elems.time + "," + price.elems.mid);
				for( int l = 0; l < runArray.length-1; ++l ){
					fw.append("," + (runArray[l].positionSide == 1 ? 1.0 : -1.0)*runArray[l].tradePrice);
				}
				fw.append("," + (runArray[runArray.length-1].positionSide == 1 ? 1.0 : -1.0)*runArray[runArray.length-1].tradePrice + "\n");
				fw.close();
				*/
				
			}catch(IOException e){
				getLogger().error("Failed opening printing values " + e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	
	/* -- REMARK: This is apparently very important, JAVA is complaining otherwise -- */
	public final String describe() {
		return "AgentModelDetail";
	}
	
	@Override
	public void reset() {
		st.initilized = false;
		st.array = null;
	}
	
	public AgentModelDetailAtomConfiguration getParameters() {
		return cfg;
	}
	
	public enum TypeSpacing{ LOG, LINEAR, BYPASS };
	
	/* -- Sets the Configuration - only two variables, slow and fast moving average arguments -- */
	public static class AgentModelDetailAtomConfiguration extends 
        Configuration<AgentModelDetailAtomConfiguration> {

		private static final long serialVersionUID = 0L; /* <- what is this for? */
		
		/* -- input: start day, end day, bin size -- */
		public DoubleProperty startThreshold;
		public DoubleProperty numberOf;
		public DoubleProperty endThreshold;
		public DoubleProperty overlapp;
		public DoubleProperty howFar;
		public StringProperty directory;
		
		
		public AgentModelDetailAtomConfiguration() {
			
			/* -- starting threshold, and number of DC threshold -- */
			startThreshold = new DoubleProperty("StartThreshold", "Start Threshold", 0.001, 0.0, Double.MAX_VALUE, false);
			numberOf = new DoubleProperty("NumberOfAgents", "Number Of Agents", 100.0, 0.0, Double.MAX_VALUE, false);
			endThreshold = new DoubleProperty("EndThreshold", "End Threshold", 0.02, 0.0, Double.MAX_VALUE, false);
			overlapp = new DoubleProperty("Overlapping", "Overlapping Thresholds", 1.0, 0.0, Double.MAX_VALUE, false);
			howFar = new DoubleProperty("HowFar", "Space Between Overlapps", 1.0, 0.0, Double.MAX_VALUE, false);
			
			
			// the thresholds are discarded here, as they are set manually.
			
			/* -- take care of this later on -- */
			directory = new StringProperty("Writing directory","WritingDirectory", "myDir", null, false);
		}
		
		public void clear() {
		}

		public String getDescription() {
			return "AgentModelDetailAtomConfig";
		}

		public String getName() {
			return "agentModelDetailAtomConfig";
		}
	}
	
	/* -- What is this for? -- */
	@Override
	public Serializable getManualStatusData() {
		return st;
	}
	@Override
	public void setManualStateData(Serializable value) {
		st = (AgentModelDetailStatus)value;
	}
}



