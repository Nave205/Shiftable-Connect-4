import java.util.Scanner;
import java.lang.Math.*;
class Connect4Shift									/*Note: can remove some brackets from fors, ifs and elses to reduce max line of code*/
{ 
	public static int[][] board = new int[6][7];				//initialize array dimensions
	public static int filled = 0;								//number of pieces
	public static int lastShifter = 0;							//0 = none, 1 = player 1, 2 = player 2
	public static int currentPlayer = 1;						//1 = player 1, 2 = player 2
	public static int lastShift = 0; 							//0 = none, 1 = left, 2 = right

	public static int winners=0; 								//0 = none, 1 = player 1, 2 = player 2, 3 = both players
	public static Scanner option = new Scanner(System.in);
	
	public static int[] unfilledColumn = {5, 5, 5, 5, 5, 5, 5}; //memoization, so far, 5 rows are unfilled, -1 if full
	
    public static void main(String args[]) 
    {
		System.out.println("Shiftable Connect-4! Player 1 (white) starts first.");
		initializeBoard();
		System.out.println();
		displayBoard(board);
		while( filled != 42 && winners == 0) 					//42 max possible moves, checks all winners
		{
			playerMove();
			displayBoard(board);
			System.out.println("\nwinners = " + winners);
			System.out.println("lastShifter = " + lastShifter);
			System.out.println("currentPlayer = " + currentPlayer);
		}
		if (winners > 2)										//both players won through shifting scenario
		{
			int newWinner = lastShifter;
			if (newWinner == 1)
				++newWinner;
			else
				--newWinner;
			System.out.println("\nPlayer " + lastShifter + " has lost, Player " + newWinner + " has won the game");
		}
		else if (winners > 0)									//either player 1 or 2 won
			System.out.println("\nPlayer " + winners + " wins the game!");
		else
			System.out.println("\nDraw");
	} 
	
	public static void initializeBoard()						//generates the 0s in the 2d-array
	{
		for(int i=0; i<6; ++i)
			for(int j=0; j<7; ++j)
				board[i][j] = 0;
	}

	public static void displayBoard(int[][] board)				//displays current state of the board
	{
		System.out.println("\nPlayer " + currentPlayer + " move");
		int r=6;												//rows 1 to 6
		char c='A';												//columns A to G
		for(int i=0; i<6; ++i)
		{
			
			System.out.print(r + " | ");
			for(int j=0; j<7; ++j)
				System.out.print(board[i][j] + " | ");
			r--;
			System.out.println();
		}
		System.out.print("    ");
		for(int k=0; k<7; ++k)
		{
			System.out.print(c + "   ");
			c++;
		}
	}
	
	public static void playerMove() {
		System.out.print("\n\t1: Drop Piece\n\t2: Shift Board\nchoice: ");
		int num = option.nextInt();
		switch (num) {
			case 1:
				dropPiece();
				break;
			case 2:
				shiftBoard();
				break;
			default:
				System.out.println("Incorrect Option");
				playerMove();
				break;
		}
	}
	
	public static void dropPiece(){
		System.out.println("Pick a column (A=1 for now) ");
		int column = option.nextInt();
		--column;													//if 1 (column A), --1 = 0
		int row = unfilledColumn[column];							//gives the lowest 0 location in terms of array row
		if (column >= 0 && column <= 6)
		{
			if (row >= 0)
			{
				board[row][column] = currentPlayer;	
				changePlayer();
				lastShift = 0;
				lastShifter = 0;
				unfilledColumn[column]--;
				filled++;
				checkLastPiece(row,column);
			}
			else
				System.out.println("Column is full, choose another");
		}
	}
	
	public static void shiftBoard(){
		System.out.print("\nChoose where to shift\n\t1: Shift Left\n\t2: Shift Right\nchoice: ");
		int direction = option.nextInt();
		switch (direction) {
			case 1:
				if(lastShift < 2)    							//if the last shift was not right
				{
					shiftLeft();	  							//then allow shifting left (left is opposite direction of right)
					shiftUnfilledColumnLeft();
					lastShifter = currentPlayer;
					changePlayer();
					lastShift = 1;
					checkWin();
				}
				else
				{
					System.out.println("Cannot shift opposite direction after shifting");
					shiftBoard();
				}
				
				break;
			case 2:
				if(lastShift != 1)   							//if the last shift was not to the left
				{
					shiftRight();	 							//then allow shifting left (right is opposite direction of left)
					shiftUnfilledColumnRight();
					lastShifter = currentPlayer;
					changePlayer();
					lastShift = 2;
					checkWin();
				}
				else
				{
					System.out.println("Cannot shift opposite direction after shifting");
					shiftBoard();
				}
				break;
			default:
				System.out.println("Incorrect Option");
				shiftBoard();
				break;
		}
	}
/*
	//memoization version	//via testing, Bruteforce might be better option, need to change code/figure out faster memoization
	public static void shiftLeft(){
		int[][] tempColumn = new int[6][1];
		int tempMax = Math.min(unfilledColumn[0],unfilledColumn[6]);		//"Max" in terms of number of pieces, row array value is always inverted
		for(int i=5; i > tempMax; --i)
		{
			tempColumn[i][0] = board[i][0];						//temp = column 1
		}
		
		for(int j=0; j<6; ++j)
			for(int i=5; i > Math.min(unfilledColumn[j],unfilledColumn[j+1]); --i)
			{
				board[i][j] = board[i][j+1];					//loop column 1 up to column 6
			}
			
		for(int i=5; i > tempMax; --i)
		{
			board[i][6] = tempColumn[i][0];						//column 7 = temp 
		}
	}
	
	public static void shiftRight(){
		int[][] tempColumn = new int[6][1];
		int tempMax = Math.min(unfilledColumn[0],unfilledColumn[6]);		//get value from which
		for(int i=5; i > tempMax; --i)
		{
			tempColumn[i][0] = board[i][6];						//temp = column 7
		}
		
		for(int j=6; j>0; --j)
			for(int i=5; i > Math.min(unfilledColumn[j],unfilledColumn[j-1]); --i)
				board[i][j] = board[i][j-1];					//loop column 1 up to column 6
			
		for(int i=5; i > tempMax; --i)
		{
			board[i][0] = tempColumn[i][0];						//column 1 = temp 
		}
	}
*/
	//Brute Force version
	public static void shiftLeft(){
		int[][] tempColumn = new int[6][1];
		
		for(int i=0; i < 6; ++i)
			tempColumn[i][0] = board[i][0];						//temp = column 1
		
		for(int j=0; j<6; ++j)
			for(int i=0; i<6; ++i)
				board[i][j] = board[i][j+1];					//loop column 1 up to column 6
			
		for(int i=0; i<6; ++i)
			board[i][6] = tempColumn[i][0];						//column 7 = temp 
	}
	
	public static void shiftRight(){
		int[][] tempColumn = new int[6][1];
		
		for(int i=0; i<6; ++i)			
			tempColumn[i][0] = board[i][6];						//temp = column 7
		
		for(int j=6; j>0; --j)									//loop column 7 to 1
			for(int i=5; i >= 0 ; --i)
				board[i][j] = board[i][j-1];		
		
		for(int i=0; i<6; ++i)
			board[i][0] = tempColumn[i][0];						//column 1 = temp
	}

	public static void shiftUnfilledColumnLeft()
	{
		int[] tempUnfilledColumn = new int[1];
		tempUnfilledColumn[0] = unfilledColumn[0];
		for(int j=0; j<6; ++j)		
			unfilledColumn[j] = unfilledColumn[j+1];
		unfilledColumn[6] = tempUnfilledColumn[0];
	}
	
	public static void shiftUnfilledColumnRight()
	{
		int[] tempUnfilledColumn = new int[1];
		tempUnfilledColumn[0] = unfilledColumn[6];
		for(int j=6; j>0; --j)	
			unfilledColumn[j] = unfilledColumn[j-1];
		unfilledColumn[0] = tempUnfilledColumn[0];
	}
	
	public static void changePlayer(){
		if (currentPlayer == 1)
			++currentPlayer;
		else
			--currentPlayer;
	}
	
	public static void checkWin()
	{
		for (int j = 0; j < 7; j++)
		{
			if (unfilledColumn[j] == 5)
				continue;
			else
			{
				for (int i=5; i != unfilledColumn[j]; i--)
				{
					if (j == 3) 								//a horizontal win is only possible on column 4, one time check
						checkHorizontal(i); 					//execute method on row 1 to 7
					if (i > 2) 									//anything beyond row 5 is imposible, i = 5,4,3,2 or row 1,2,3,4
					{
						checkVertical(i,j);						//execute method on row 1 to 4
						if (j < 3)
							checkDiaRight(i,j); 				//execute methods on row 1 to 4 and columns 1 to 3
						else if (j > 3)
							checkDiaLeft(i,j); 					//execute methods on row 1 to 4 and columns 5 to 7
						else
						{
							checkDiaRight(i,j); 				//execute methods on row 1 to 4 and column 4
							checkDiaLeft(i,j);
						}
					}
				}
			}
		}
		
	}
	
	public static void checkHorizontal(int row) 				//from right to left, only columns 1 to 4
	{
		int player = board[row][3];
		int chain=0;
		if (player == 0)
			return;
		else
		{
			for (int column = 0; column < 7; column++)
			{
				if (board[row][column] != player)
				{
					if (column < 5)
					{
						chain = 0;
						continue;
					}
					else
						break;
				}
				else
				{
					chain++;
					if (chain == 4)
						winners += player;
				}
			}
		}
	}
	
	public static void checkOverallHorizontal(int row, int column)
	{
		int player = board[row][column];
		int chain=0;
		if (player != board[row][3])
			return;
		else
		{
			for (int i=0, x = column+1; i<4; i++,x++)
			{
				if (x<=6)
				{
					if (board[row][x] == player)
						chain++;
					else
						break;
				}
				else
					break;
			}
			if (chain >= 3)
				winners += player;
			else
			{
				for (int i=0, x = column-1; i<4; i++,x--)
				{
					if (x>=0)
					{
						if (board[row][x] == player)
							chain++;
						else
							break;
					}
					else
						break;
				}
				if (chain >= 3)
					winners += player;
			}
		}
	}
	
	public static void checkVertical(int row, int column) 		//from bottom to top, only rows 1 to 3
	{
		int player;
		int chain = 0;
		player = board[row][column];
		if (player == 0)
			return;
		else
		{
			while (row >= 0)
			{
				if (board[row--][column] == player)
					chain++;
				else
					break;
			}
			if (chain == 4)
				winners += player;	//impossible to have 2 winners on shift (shifting is horizontal, vertical has no horizontal characteristics)
		}
	}
	
	public static void checkOverallVertical(int row, int column) 		//from bottom to top, only rows 3 to 6
	{
		int player;
		int chain = 0;
		player = board[row][column];
		for (int i=0, y = row+1; i<4; i++,y++)
		{
			if (y<=5)
			{
				if (board[y][column] == player)
					chain++;
				else
					break;
			}
			else
				break;
		}
		if (chain >= 3)
			winners += player;
		else
		{
			for (int i=0, y = row-1; i<4; i++,y--)
			{
				if (y>=0)
				{
					if (board[y][column] == player)
						chain++;
					else
						break;
				}
				else
					break;
			}
			if (chain >= 3)
				winners += player;
		}
	}
	public static void checkDiaRight(int row, int column)  		//only rows 1 to 3 and columns 1 to 4
	{
		int player;
		int chain = 0;
		player = board[row][column];
		if (player == 0)
			return;
		else
		{
			while (row >= 0 && column <= 6)
			{
				if (board[row--][column++] == player)
					chain++;
				else
					break;
			}
			if (chain == 4)
				winners += player;
		}
	}
	
	public static void checkOverallDiaRight(int row, int column)
	{
		int player;
		int chain = 0;
		player = board[row][column];
		for (int i=0, y = row+1, x = column-1; i<4; i++,y++,x--)  //down to the left
		{
			if (y<=5 && x>=0)
			{
				if (board[y][x] == player)
					chain++;
				else
					break;
			}
			else
				break;
		}
		if (chain >= 3)
			winners += player;
		else
		{
			for (int i=0, y = row-1,x=column+1; i<4; i++,y--,x++)	//up to the right
			{
				if (y>=0 && x<=6)
				{
					if (board[y][x] == player)
						chain++;
					else
						break;
				}
				else
					break;
			}
			if (chain >= 3)
				winners += player;
		}
	}
	
	public static void checkDiaLeft(int row, int column)
	{
		int player;
		int chain = 0;
		player = board[row][column];
		if (player == 0)
			return;
		else
		{
			while (row >= 0 && column >= 0)
			{
				if (board[row--][column--] == player)
					chain++;
				else
					break;
			}
			if (chain == 4)
				winners += player;
		}
	}

	public static void checkOverallDiaLeft(int row, int column)
	{
		int player;
		int chain = 0;
		player = board[row][column];
		for (int i=0, y = row+1, x = column+1; i<4; i++,y++,x++)  //down to the right
		{
			if (y<=5 && x<=6)
			{
				if (board[y][x] == player)
					chain++;
				else
					break;
			}
			else
				break;
		}
		if (chain >= 3)
			winners += player;
		else
		{
			for (int i=0, y = row-1,x=column-1; i<4; i++,y--,x--)	//up to the left
			{
				if (y>=0 && x>=0)
				{
					if (board[y][x] == player)
						chain++;
					else
						break;
				}
				else
					break;
			}
			if (chain >= 3)
				winners += player;
		}
	}
	
	public static void checkLastPiece(int row, int column)
	{
		checkOverallHorizontal(row, column);
		if (winners > 0)
			return;
		checkOverallVertical(row, column);
		if (winners > 0)
			return;
		checkOverallDiaLeft(row, column);
		if (winners > 0)
			return;
		checkOverallDiaRight(row, column);
	}
	
	//=========================== Incomplete/Inefficient future AI Part ================================
	
	public static int checkIfWin(int column){
		aiDropPiece(column);
		checkWin();
		if (winners > 0)
		{
			winners = 0;
			return 500;
		}
		else
			return 0;
	}
	
	//did not have time to edit other methods
	public static void aiDropPiece(int column){
		int row = unfilledColumn[column];							//gives the lowest 0 location in terms of array row
		if (column >= 0 && column <= 6)
		{
			if (row >= 0)
			{
				board[row][column] = currentPlayer;	
				changePlayer();
				lastShift = 0;
				lastShifter = 0;
				unfilledColumn[column]--;
				filled++;
				checkLastPiece(row,column);
			}
			else
				System.out.println("Column is full, choose another");
		}
	}
	
	public static int evaluate (int i, int j, int k, int l, int m){
		Object[] store = storeBoard();
		aiDropPiece(i); //max
		aiDropPiece(j); //min
		aiDropPiece(k); //max 
		aiDropPiece(l);	//min
		int eval = checkIfWin(m);				//child of min part, m=column evaluation, alpha-beta pruning?
		resetBoard(store);
		return eval;
	}
	
	public static Object[] storeBoard (){
		int[][] origBoard = new int[6][7];
		for (int row=0; row<6;row++)
			for (int column=0; column<7;column++)
				origBoard[row][column] = board[row][column];
		int origFilled = filled;	
		int origLastShifter = lastShifter;	
		int origCurrentPlayer = currentPlayer;
		int origLastShift = lastShift;
		int origWinners = winners; 				
		int[] origUnfilledColumn = new int[7];
		for (int column=0; column<7;column++)
			origUnfilledColumn[column] = unfilledColumn[column];
		
		Object[] data = new Object[7];
		data[0] = origBoard;
		data[1] = origFilled;
		data[2] = origLastShifter;
		data[3] = origCurrentPlayer;
		data[4] = origLastShift;
		data[5] = origWinners;
		data[6] = origUnfilledColumn;
		return data;
	}
	
	public static void resetBoard (Object data[]){
		board = (int[][]) data[0];
		filled = (int) data[1];	
		lastShifter = (int) data[2];	
		currentPlayer = (int) data[3];
		lastShift = (int) data[4];
		winners = (int) data[5] ;				
		unfilledColumn = (int[]) data[6];
	}
	
} 