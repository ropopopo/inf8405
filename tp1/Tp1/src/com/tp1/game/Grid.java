package com.tp1.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Pair;
import com.tp1.framework.Input.TouchEvent;
import com.tp1.framework.implementation.AndroidGame;
import com.tp1.game.BejewelloMenu.Mode;

public class Grid
{
	//Variables
	final int numberOfColumns = 8, numberOfLines = 8;
	Gem[][] gems;
	int _x = 10, _y = 80;
	AndroidGame _game;
	int _numberOfSelectedGems = 0;
	int _score = 0;
	int _movesDone = 0;
	Gem gem1 = null, gem2 = null;
	private static Grid _instance;
	boolean _animSwitch = false;
	boolean _goodSwitch;
	boolean _notFinished;
	boolean _switchingGems = false;
	boolean _foundALine;
	boolean _updatingGems = false;
	int _potentialLines = 0;
	BejewelloMenu.Mode _mode;
	Integer _time = 60;
	Timer _timer;
	boolean _gameOver = false;
	int _movesRemaining = 10;
	boolean _isPaused = false;
	
	
	public Grid()
	{
		_instance = this;
		_game = Bejewello.getGame();
		_mode = BejewelloMenu._mode;
		gems = new Gem[numberOfColumns][numberOfLines];
		for(int i=0; i<numberOfColumns; i++)
		{
			for(int j=0; j<numberOfLines; j++)
			{
				gems[i][j] = new Gem();
			}
		}
		for(int i=0; i<numberOfColumns; i++)
		{
			for(int j=0; j<numberOfLines; j++)
			{
				gems[i][j].assignPosition(_x, _y, i, j);
				
				gems[i][j].initNeighbors();
				gems[i][j]._topNeighbor = inGrid(i,j-1) ? gems[i][j-1] : null;
				gems[i][j]._bottomNeighbor = inGrid(i,j+1) ? gems[i][j+1] : null;
				gems[i][j]._leftNeighbor = inGrid(i-1,j) ? gems[i-1][j] : null;
				gems[i][j]._rightNeighbor = inGrid(i+1,j) ? gems[i+1][j] : null;
			}
		}	
		
		//so that we don't have a line of 3 of the same type
		fixGemTypes();
		updatePotentialLines();
		
		if(_mode == Mode.CHRONO)
		{
			_timer = new Timer();
			_timer.schedule(new UpdateTimer(), 0, 1000);
		}
	}
	
	//Singleton
	public static Grid getInstance()
	{
		return (_instance == null) ? new Grid() : _instance;
	}
	
	//make sure we don't have a line of 3 of the same type
	public void fixGemTypes()
	{
		boolean fixed = false;
		
		while(!fixed)
		{
			fixed = true;
			for(Gem[] column : gems)
			{
				for(Gem g : column)
				{
					//vertical lines
					if(g.topNeighbor() != null && 
					   g.topNeighbor().topNeighbor() != null && 
					   g.topNeighbor()._gemType == g._gemType && 
					   g.topNeighbor().topNeighbor()._gemType == g._gemType)
					{
						g.topNeighbor().changeGemType();
						fixed = false;
					}
					if(	g.topNeighbor() != null && 
						g.bottomNeighbor() != null &&
						g.topNeighbor()._gemType == g._gemType && 
						g.bottomNeighbor()._gemType == g._gemType)
					{
						g.changeGemType();
						fixed = false;
					}
					if(	g.bottomNeighbor() != null && 
						g.bottomNeighbor().bottomNeighbor() != null &&
						g.bottomNeighbor()._gemType == g._gemType && 
						g.bottomNeighbor().bottomNeighbor()._gemType == g._gemType)
					{
						g.bottomNeighbor().changeGemType();
						fixed = false;
					}
					
					//horizontal lines
					if(g.leftNeighbor() != null && 
					   g.leftNeighbor().leftNeighbor() != null && 
					   g.leftNeighbor()._gemType == g._gemType && 
					   g.leftNeighbor().leftNeighbor()._gemType == g._gemType)
					{
						g.leftNeighbor().changeGemType();
						fixed = false;
					}
					if(g.leftNeighbor() != null && 
					   g.rightNeighbor() != null &&
					   g.leftNeighbor()._gemType == g._gemType && 
					   g.rightNeighbor()._gemType == g._gemType)
					{
						g.changeGemType();
						fixed = false;
					}
					if(g.rightNeighbor() != null && 
					   g.rightNeighbor().rightNeighbor() != null &&
					   g.rightNeighbor()._gemType == g._gemType && 
					   g.rightNeighbor().rightNeighbor()._gemType == g._gemType)
					{
						g.rightNeighbor().changeGemType();
						fixed = false;
					}
				}
			}
		}
	}

	//check if position is inside the grid
	public boolean inGrid(int i, int j)
	{
		if(i >= 0 && i < numberOfColumns && j >= 0 && j < numberOfLines)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//checks to see the lines around these gems make a good line
	public void checkLinesAroundGem(List<Gem> list)
	{
		_updatingGems = true;
		_foundALine = false;
		_notFinished = true;
		for(Gem g : list)
		{
			if(!g.isMarked())
			{
				Gem temp = g;
				List<Gem> gemListH = new ArrayList<Gem>();
				List<Gem> gemListV = new ArrayList<Gem>();
				gemListH.add(g);
				gemListV.add(g);
				
				//horizontal
				while (	temp != null && 
						temp.leftNeighbor() != null &&
						temp.leftNeighbor()._gemType == g._gemType)
				{
					temp = temp.leftNeighbor();
					gemListH.add(temp);
				}
				temp = g;
				while (	temp != null && 
						temp.rightNeighbor() != null &&
						temp.rightNeighbor()._gemType == g._gemType)
				{
					temp = temp.rightNeighbor();
					gemListH.add(temp);
				}	
				//vertical
				temp = g;
				while (	temp != null && 
						temp.topNeighbor() != null && 
						temp.topNeighbor()._gemType == g._gemType)
				{
					temp = temp.topNeighbor();
					gemListV.add(temp);
				}
				temp = g;
				while (	temp != null && 
						temp.bottomNeighbor() != null &&
						temp.bottomNeighbor()._gemType == g._gemType)
				{
					temp = temp.bottomNeighbor();
					gemListV.add(temp);
				}
				
				//we found an horizontal line
				if(gemListH.size() >= 3)
				{
					_foundALine = true;
					_goodSwitch = true;
					_score += gemListH.size()*100;
					_movesDone ++;
					if(gemListH.size() > 3)
					{
						_score -= (gemListH.size() - 3) * 50;
					}
					Assets.click.play(30);
		
					for(Gem g2 : gemListH)
					{
						g2.mark();
						g2.highlight();
						g2.disappear();
					}
				}
				
				//we found a vertical line
				if (gemListV.size() >= 3)
				{
					_foundALine = true;
					_goodSwitch = true;
					_score += gemListV.size()*100;
					_movesDone ++;
					if(gemListV.size() > 3)
					{
						_score -= (gemListV.size() - 3) * 50;
					}
					Assets.click.play(30);	
					
					for(Gem g2 : gemListV)
					{
						g2.mark();
						g2.highlight();
						g2.disappear();
					}
				}
			}
		}
		_notFinished = false;
		if (_foundALine)
		{
			//this waits until all the animations are finished
			waitUntilReady();	
		}
		
		_updatingGems = false;
	}
	
	//this waits until all the animations are finished
	public void waitUntilReady()
	{
		try
		{
			Thread.sleep(500);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		boolean ready = false;
		while(!ready)
		{
			ready = true;
	        for(Gem[] column : gems)
	        {
	        	for (Gem g : column)
	        	{
	        		if(g._disappearing || g.moving())
	        		{
	        			ready = false;
	        		}
	        	}
	        }
		}
		moveLines();
	}
	
	
	public void finnishMovingLines(final List<Pair<List<Gem>, Integer>> lines, final List<Pair<Integer, Integer>> freeSpace)
	{
		List<Gem> list = new ArrayList<Gem>();
    	
    	while (somethingMoving())
    	{
    		try
			{
				Thread.sleep(500);
			} 
    		catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    	}
    	
    	//move gems where there is free space
    	for(Gem[] column : gems)
		{
			for (Gem g : column)
			{
				if(g._disappeared)
				{	
					list.add(g);
					if(freeSpace.size() > 0)
					{
						try{
    					g._x = freeSpace.get(0).first;
    					g._y = freeSpace.get(0).second;
    					freeSpace.remove(0);}
						catch(Exception e)
						{
							System.out.println(e);
						}
					}
					else
					{
						System.out.println("caca");
					}
				}
			}
		}
    	
    	//update neighbors
    	for(Pair<List<Gem>, Integer> line : lines)
		{
			for(Gem g : line.first)
			{
				list.add(g);
				g.updateNeighbors();
				g.updateNeighborsOfNeighbors();
			}
		}
    	
    	//update neighbors and make disapeared gems visible again
    	for(Gem[] column : gems)
		{
			for (Gem g : column)
			{
				if(g._disappeared)
				{		        					
					g.changeGemType();
					g.reappear();
					g.updateNeighbors();
					g.updateNeighborsOfNeighbors();
				}
			}
		}
    	
    	for(Gem[] col : gems)
		{
			for (Gem g : col)
			{
				g.unMark();
				g._firstTime = true;
			}
		}    	
	}

	//move the gems above a good line down 1 
	public void moveLines()
	{	
		List<Pair<List<Gem>, Integer>> lines = new ArrayList<Pair<List<Gem>, Integer>>();
		List<Pair<Integer, Integer>> freeSpace = new ArrayList<Pair<Integer, Integer>>();
		
		//add gems to move to a list
		//get the free space generated by the lines moving down
		for (Gem[] column : gems)
		{
			for (Gem g : column)
			{		
				int lineLength = 0;
				if(g.bottomNeighbor() != null && !g._disappeared && g.bottomNeighbor()._disappeared)
				{
					List<Gem> line = new ArrayList<Gem>();
					Gem temp = g;
					
					while(temp.bottomNeighbor() != null && temp.bottomNeighbor()._disappeared)
					{
						temp = temp.bottomNeighbor();
						lineLength++;
					}
					
					temp = g;
					line.add(g);
					while(temp.topNeighbor() != null)
					{
						temp = temp.topNeighbor();
						if(!temp._disappeared)
						{
							line.add(temp);
						}
					}
					
					for (int i = 0; i < lineLength; i++)
					{
						addFreeSpace(freeSpace, temp._x, temp._y);
						temp = temp.bottomNeighbor();
					}
					lines.add(new Pair<List<Gem>, Integer>(line, lineLength));
				}
				else if(g._disappeared && g.topNeighbor() == null)
				{
					Gem temp = g;
					freeSpace.add(new Pair<Integer, Integer> (temp._x, temp._y));
					while(temp.bottomNeighbor()._disappeared)
					{
						temp = temp.bottomNeighbor();
						addFreeSpace(freeSpace, temp._x, temp._y);
					}
					
				}
			}
		}
		
		//move the disappeared gems
		for(Gem[] column : gems)
		{
			for (Gem g : column)
			{
				if(g._disappeared)
				{		        					
					g._x = -1000;
					g._y = -1000;
				}
			}
		}
		//move lines down
		for(Pair<List<Gem>, Integer> line : lines)
		{			
			moveLineDown(line);
		}
		
		//reposition disappeared gems, make them reappear and update all neighbors that were in a good line
		finnishMovingLines(lines, freeSpace);
	}
	
	public void addFreeSpace(List<Pair<Integer, Integer>> freeSpace, int x, int y)
	{
		for (Pair<Integer, Integer> p : freeSpace)
		{
			if(p.first == x && p.second == y)
			{
				addFreeSpace(freeSpace, x, y + Gem._height);
				return;
			}
		}
		
		freeSpace.add(new Pair<Integer, Integer>(x, y));
		return;
	}
	
	//actually move the gems
	public void moveLineDown(Pair<List<Gem>, Integer> line)
	{
		if(line.first.size() > 0)
		{
			for (Gem g : line.first)
			{
				g.moveTo(g._x, g._y + line.second * Gem._height);
			}
		}
	}
	
	//checks if a gem is moving in the grid
	public boolean somethingMoving()
	{
		for(Gem[] column : gems)
		{
			for (Gem g : column)
			{
				if(g.moving())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int getScore()
	{
		return (_score);
	}
	
	public void pauseTimer()
	{
		_isPaused = true;
	}
	
	public void unPauseTimer()
	{
		_isPaused = false;
	}
	
	//updates the game with score, moves, time, gameover
	public void updateUI()
	{
		_game.setMoves(_movesDone);
		_game.setScore(_score);
		
		//Si la partie est termin�e
		if(_gameOver)
		{
			_game.saveScore();
			_game.setScreen(ScreenManager.getInstance().getGameOverScreen());	//montre l'�cran de fin
		}
		
		if(_mode == Mode.CHRONO)
		{
			_game.setRemaining(_time, "chrono");
		}
		else
		{
			_game.setRemaining(_movesRemaining, "moves");
		}
	}
	
	//this function runs at every one second
	class UpdateTimer extends TimerTask {
	    public void run() 
	    {
	    	if (!_isPaused)
	    	{
		       _time--; 
		       if(_time <= 0)
		       {
		    	   _gameOver = true;
		       }
	    	}
	    }
	 }
	
	//get the number of potential lines the player can make
	public void updatePotentialLines()
	{
		_potentialLines = 0;
		for(Gem[] col : gems)
		{
			for (Gem g : col)
			{
				Gem topLeft = (g.leftNeighbor() != null && g.leftNeighbor().topNeighbor() != null) ? g.leftNeighbor().topNeighbor() : null;
				Gem topRight = (g.rightNeighbor() != null && g.rightNeighbor().topNeighbor() != null) ? g.rightNeighbor().topNeighbor() : null;
				Gem bottomLeft = (g.leftNeighbor() != null && g.leftNeighbor().bottomNeighbor() != null) ? g.leftNeighbor().bottomNeighbor() : null;
				Gem bottomRight = (g.rightNeighbor() != null && g.rightNeighbor().bottomNeighbor() != null) ? g.rightNeighbor().bottomNeighbor() : null;
				
				//left
				if(g.leftNeighbor() != null && g.leftNeighbor()._gemType == g._gemType &&
						g.leftNeighbor().leftNeighbor() != null && g.leftNeighbor().leftNeighbor()._gemType == g._gemType)
					_potentialLines++;
				
				//right
				if(g.rightNeighbor() != null && g.rightNeighbor()._gemType == g._gemType &&
						g.rightNeighbor().rightNeighbor() != null && g.rightNeighbor().rightNeighbor()._gemType == g._gemType)
					_potentialLines++;
				
				//top
				if(g.topNeighbor() != null && g.topNeighbor()._gemType == g._gemType &&
						g.topNeighbor().topNeighbor() != null && g.topNeighbor().topNeighbor()._gemType == g._gemType)
					_potentialLines++;
				
				//bottom
				if(g.bottomNeighbor() != null && g.bottomNeighbor()._gemType == g._gemType &&
						g.bottomNeighbor().bottomNeighbor() != null && g.bottomNeighbor().bottomNeighbor()._gemType == g._gemType)
					_potentialLines++;
				
				
				//top left
				if(topLeft != null && topLeft._gemType == g._gemType)
				{
					if(topLeft.leftNeighbor() != null && topLeft.leftNeighbor()._gemType == g._gemType || 
						(topRight != null && topRight._gemType == g._gemType))
						_potentialLines++;
					else if(topLeft.topNeighbor() != null && topLeft.topNeighbor()._gemType == g._gemType || 
							(bottomLeft != null && bottomLeft._gemType == g._gemType))
						_potentialLines++;
				}
				
				//top right
				if(topRight != null && topRight._gemType == g._gemType)
				{
					if(topRight.rightNeighbor() != null && topRight.rightNeighbor()._gemType == g._gemType || 
						(topLeft != null && topLeft._gemType == g._gemType))
						_potentialLines++;
					else if(topRight.topNeighbor() != null && topRight.topNeighbor()._gemType == g._gemType || 
							(bottomRight != null && bottomRight._gemType == g._gemType))
						_potentialLines++;
				}
				
				//bottom left
				if(bottomLeft != null && bottomLeft._gemType == g._gemType)
				{
					if(bottomLeft.leftNeighbor() != null && bottomLeft.leftNeighbor()._gemType == g._gemType || 
						(bottomRight != null && bottomRight._gemType == g._gemType))
						_potentialLines++;
					else if(bottomLeft.bottomNeighbor() != null && bottomLeft.bottomNeighbor()._gemType == g._gemType ||
							(topLeft != null && topLeft._gemType == g._gemType))
						_potentialLines++;
				}
				
				//bottom right
				if(bottomRight != null && bottomRight._gemType == g._gemType)
				{
					if(bottomRight.rightNeighbor() != null && bottomRight.rightNeighbor()._gemType == g._gemType || 
						(bottomLeft != null && bottomLeft._gemType == g._gemType))
						_potentialLines++;
					else if(bottomRight.bottomNeighbor() != null && bottomRight.bottomNeighbor()._gemType == g._gemType || 
							(topRight != null && topRight._gemType == g._gemType))
						_potentialLines++;
				}
			}
		}
	}

	//this is launched when there is user input
	public void update(TouchEvent event)
	{				
		if(_gameOver)
			return;
		for(Gem[] column : gems)
		{
			for(Gem g : column)
			{
				if(_switchingGems == false)
					g.update(event);
			}
		}	
		
		//switch gems
		if(_switchingGems == false && getNumberOfSelectedGems() == 2)
		{
			for(Gem[] column : gems)
			{
				for(Gem g : column)
				{					
					if(g.isSelected())
					{
						if(gem1 == null)
						{
							gem1 = g;
						}
						else if(gem2 == null)
						{
							gem2 = g;
						}
					}
				}
			}
			
			if(gem1.isNeighbor(gem2))
			{
				switchGems(gem1, gem2);
				deselectAll();
				gem1 = null;
				gem2 = null;
				_numberOfSelectedGems = 0;
			}
			else
			{
				deselectAll();
				gem1 = null;
				gem2 = null;
				_numberOfSelectedGems = 0;
			}
		}
		else if (getNumberOfSelectedGems() > 2)
		{
			deselectAll();
		}
	}

	//returns the number of selected gems
	public int getNumberOfSelectedGems()
	{
		int number = 0;
		for(Gem[] column : gems)
		{
			for(Gem g : column)
			{
				if(g.isSelected())
				{
					number++;
				}
			}
		}
		return number;
	}
	
	//returns the timer
	public Timer getTimer()
	{
		return(_timer);
	}
	
	//switch 2 gems 
	public void switchGems(final Gem gem1, final Gem gem2)
	{			
		_goodSwitch = false;
		gem1.moveTo(gem2._x, gem2._y);
		gem2.moveTo(gem1._x, gem1._y);
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				_switchingGems = true;
				checkIfGoodSwitch(gem1, gem2);
				_switchingGems = false;
			}
		};
		new Thread(r).start();
	}
	
	//check if the switch is a valid one (forms a valid line)
	public void checkIfGoodSwitch(Gem gem1, Gem gem2)
	{
    	while (somethingMoving())
    	{
    		try
			{
				Thread.sleep(200);
			} 
    		catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    	}
    	List<Gem> list = new ArrayList<Gem>();
    	list.add(gem1);
    	list.add(gem2);
		checkLinesAroundGem(list);
		
    	if(!_goodSwitch)
    	{
    		//if its not a good switch
    		gem1.moveTo(gem2._x, gem2._y);
    		gem2.moveTo(gem1._x, gem1._y);
    	}
    	else
    	{
    		//the switch is valid
    		List<Gem> allGems = new ArrayList<Gem>();
			for(Gem[] col : gems)
			{
				for(Gem g : col)
				{
					allGems.add(g);
				}
			}
    		while(_foundALine)
    		{	
    			if(!_updatingGems && !somethingMoving())
    			{
    				checkLinesAroundGem(allGems);
    			}
    			try
				{
					Thread.sleep(200);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
    		}
    		
    		_movesRemaining--;
    		_gameOver = (_movesRemaining > 0) ? false : true;
    	}
    	try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
    	updatePotentialLines();
	}

	public void deselectAll()
	{
		for(Gem[] column : gems)
		{
			for(Gem g : column)
			{
				g.deselect();
			}
		}	
	}
	
	public void resetScore()
	{
		_score = 0;
	}

	//the loop for graphics
	public void paint()
	{
		for(Gem[] column : gems)
		{
			for(Gem g : column)
			{
				if(!_gameOver)
					g.paint();
			}
		}	
	}
}
