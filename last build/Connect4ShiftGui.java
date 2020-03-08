import java.util.Scanner;
import java.lang.Math.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.Modality;
import javafx.stage.Window;

import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class Connect4ShiftGui extends Application				/*Note: can remove some brackets from fors, ifs and elses to reduce max line of code*/
{ 
	public static int[][] board = new int[6][7];				//initialize array dimensions
	public static int filled = 0;								//number of pieces
	public static int lastShifter = 0;							//0 = none, 1 = player 1, 2 = player 2
	public static int currentPlayer = 1;						//1 = player 1, 2 = player 2
	public static int lastShift = 0; 							//0 = none, 1 = left, 2 = right
	public static int winners=0; 								//0 = none, 1 = player 1, 2 = player 2, 3 = both players
	public static Scanner option = new Scanner(System.in);
	
	public static int[] unfilledColumn = {5, 5, 5, 5, 5, 5, 5}; //memoization, so far, 5 rows are unfilled
	
	private static final int TILE_SIZE = 80;
	
	private double sceneHeight = 567;
	private double sceneWidth = 768;
	
	private double resultHeight = 90;
	private double resultWidth = 350;
	
	private double windowHeight = 700;
	
    private int m = 6;
    private int n = 7;

    double gridHeight = sceneHeight / m;
    double gridWidth = sceneWidth / n;
	
    MyNode[][] playfield = new MyNode[m][n];
	private Group playerPiece = new Group();
	
    public static void main(String args[]) 
    {
		System.out.println("Shiftable Connect-4! Player 1 (white) starts first.");
		initializeBoard();
		launch(args);
		System.out.println();
		System.out.println("Final Board State");
		displayBoard(board);
		System.out.println(declareWinner());
		System.out.println();
	} 
	
	public static String declareWinner(){
		String message;
		if (winners > 2)										//both players won through shifting scenario
		{
			int newWinner = lastShifter;
			if (newWinner == 1)
				++newWinner;
			else
				--newWinner;
			message = ("Player " + lastShifter + " (" + playerTurn(lastShifter) + ") has lost, Player " + newWinner + " (" + playerTurn(newWinner) + ") has won the game");
		}
		else if (winners > 0)									//either player 1 or 2 won
			message = ("Player " + winners + " (" + playerTurn(winners) + ") wins the game!");
		else
			message = "Draw";
		return message;
	}
	
	public static void initializeBoard()						//generates the 0s in the 2d-array
	{
		for(int i=0; i<6; ++i)
			for(int j=0; j<7; ++j)
				board[i][j] = 0;
	}

	@Override
    public void start(Stage primaryStage) {

        Group root = new Group();
		refreshBoard();
        Scene scene = new Scene( root, sceneWidth, windowHeight);
		
		root.getChildren().add(playerPiece);
		
		Button buttonL = new Button("<-");
		buttonL.setTranslateX(1.5*gridWidth);
		buttonL.setTranslateY(6.5*gridHeight);
		buttonL.setOnMouseClicked((e) -> {
			shiftBoard(1); refreshBoard();
			if(winners>0 || filled == 42)
			{
				resultScene(primaryStage, Modality.APPLICATION_MODAL);
			}
		});

		Button buttonR = new Button("->");
		buttonR.setTranslateX(5.5*gridWidth);
		buttonR.setTranslateY(6.5*gridHeight);
		buttonR.setOnMouseClicked((e) -> {
			shiftBoard(2); refreshBoard();
			if(winners>0 || filled == 42)
			{
				resultScene(primaryStage, Modality.APPLICATION_MODAL);
			}
		});
		root.getChildren().addAll(buttonL, buttonR);
		
		scene.setOnMouseClicked((e) -> {if(winners>0 || filled == 42){resultScene(primaryStage, Modality.APPLICATION_MODAL);}});
        primaryStage.setScene(scene);
		primaryStage.setTitle("Connect 4 Shift");
		primaryStage.setOnCloseRequest(e->{Platform.exit();});
		root.getChildren().addAll(interactColumns());
		
        primaryStage.show();
    }

	public void resultScene(Window primaryStage, Modality modality) {
		
		final Stage result = new Stage();
		result.initOwner(primaryStage);
        result.initModality(modality);
		Label label = new Label(declareWinner());
		label.setTranslateX(25);
		label.setTranslateY(25);
		Group root = new Group();
		Scene resultScene = new Scene(root, resultWidth, resultHeight);
		root.getChildren().addAll(label);
		result.setScene(resultScene);
		result.setTitle("Result");
		result.setOnCloseRequest(e->{Platform.exit();});
		result.show();
	}
	
	public static String playerTurn(int player) {
		if (player==1)
			return "Red";
		else
			return "Yellow";
	}
	
	public void changeValue (int column){
		int row = unfilledColumn[column];							//gives the lowest 0 location in terms of array row
		if (row >= 0)
		{
			board[row][column] = currentPlayer;	
			changePlayer();
			lastShift = 0;
			lastShifter = 0;
			unfilledColumn[column]--;
			filled++;
			checkLastPiece(row, column);
			refreshBoard();
		}
		else
			System.out.println("Column is full, choose another");
    }
	
	public void refreshBoard (){
        for( int i=0; i < m; i++) {
            for( int j=0; j < n; j++) {
                MyNode node = new MyNode(i, j, j * gridWidth, i * gridHeight, gridWidth, gridHeight);
				playfield[i][j] = node;
                playerPiece.getChildren().add(node);
            }
        }
		Label label = new Label(playerTurn(currentPlayer) + "'s turn to move");
		Rectangle rect = new Rectangle(2*gridWidth, gridHeight);
		rect.setFill(Color.WHITE);
		rect.setTranslateX(3*gridWidth);
		rect.setTranslateY(6.5*gridHeight);
		label.setTranslateX(3*gridWidth);
		label.setTranslateY(6.5*gridHeight);
		playerPiece.getChildren().addAll(rect,label);
	}
	
	public static class MyNode extends StackPane {

        public MyNode(int i, int j, double x, double y, double width, double height) {
            Rectangle rectangle = new Rectangle( width, height);
            rectangle.setStroke(Color.BLACK);
            rectangle.setFill(Color.web("#1471C1"));
			Circle circle = new Circle(TILE_SIZE / 2);
			
			if (board[i][j] == 0)
				circle.setFill(Color.WHITE);
			else if (board[i][j] == 1)
				circle.setFill(Color.RED);
			else
				circle.setFill(Color.YELLOW);

            setTranslateX( x);
            setTranslateY( y);
            getChildren().addAll(rectangle, circle);
        }
    }

	private List<Rectangle> interactColumns() {
        List<Rectangle> list = new ArrayList<>();

        for (int x = 0; x < 7; x++) {
            Rectangle rect = new Rectangle(gridWidth, sceneHeight);
            rect.setTranslateX(x * gridWidth);
            rect.setFill(Color.TRANSPARENT);
            rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
			
            final int column = x;
            rect.setOnMouseClicked(e -> changeValue(column));
            list.add(rect);
        }
        return list;
    }
	
	public static void displayBoard(int[][] board)				//displays current state of the board
	{
		//System.out.println("\nPlayer " + currentPlayer + " move");
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
		System.out.println();
	}
	
	public static void shiftBoard(int direction){
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
					System.out.println("Cannot shift opposite direction after shifting");
				
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
					//shiftBoard();
				}
				break;
			default:
				System.out.println("Incorrect Option");
				//shiftBoard();
				break;
		}
	}
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
		{
			unfilledColumn[j] = unfilledColumn[j-1];
		}
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
					{
						checkHorizontal(i); 					//execute method on row 1 to 7
					}
					if (i > 2) 									//anything beyond row 5 is imposible, i = 5,4,3,2 or row 1,2,3,4
					{
						checkVertical(i,j);						//execute method on row 1 to 4
						if (j < 4)
							checkDiaRight(i,j); 				//execute methods on row 1 to 4 and columns 1 to 3
						else if (j > 4)
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
	
	public static void checkDiaLeft(int row, int column) 		//only rows 1 to 3 and columns 4 to 7
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
} 