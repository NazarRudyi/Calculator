package ua.nr.calculator;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CalculatorActivity extends Activity {
	
	public final static String TAG = "ua.nr.calculator.Calculator_tag";
	
	
	private EditText txtResult;
	
	private Button btnAdd, 
	               btnDivide, 
	               btnMultiply, 
	               btnSubtract;	
	
	private Add add = new Add();
	private Divide div = new Divide();
	private Multiply mul = new Multiply();
	private Subtract sub = new Subtract();
	
	private OperationType operType;
	
	private Context context = new Context();
	
	
	
	private EnumMap<Symbol, Object> commands = new EnumMap<>(Symbol.class);
	private Map<OperationType, Double> forCalc = new HashMap<>();

	
	private static int currentTheme = R.style.CalculationTheme;
	private static double currentResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTheme(currentTheme);
		
		setContentView(R.layout.calculator);
		
		txtResult = (EditText)findViewById(R.id.txtResult);
		txtResult.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length()>0){
				     currentResult = Double.valueOf(s.toString().replace(',', '.'));	
			    }else{
			    	currentResult = 0;
			    }
			}
		});
		
		
		showResult(currentResult);
		
		
		btnAdd = (Button)findViewById(R.id.btnPlus);
		btnDivide = (Button)findViewById(R.id.btnDivide);
		btnMultiply = (Button)findViewById(R.id.btnMultiply);
		btnSubtract = (Button)findViewById(R.id.btnMinus);
		
		// до кожної кнопки додаємо тип операції
		btnAdd.setTag(OperationType.ADD);
		btnDivide.setTag(OperationType.DIVIDE);
		btnMultiply.setTag(OperationType.MULTIPLY);
		btnSubtract.setTag(OperationType.SUBTRACT);
		
	}
	
	private ActionType lastAction;

	
	public void buttonClick(View v){
		
		switch (v.getId()){
		
		case R.id.btnPlus:
		case R.id.btnDivide:
		case R.id.btnMultiply:
		case R.id.btnMinus:{// якщо нажата одна із кнопок операції
			
			// получаємо тип операції
			operType = (OperationType)v.getTag();
			
			// перевіряємо чи була нажата знову кнопка операції, якщо так, то записуєм в наш EnumMap нову операцію(стара відміняється)
			if (lastAction == ActionType.OPERATION){
				commands.put(Symbol.OPERATION, operType);
				return;
			}
			
			// якщо раніше ця кнопка не була нажата, тобто користувач нажав кнопку операції перший раз, тоді робимо слідуюче
			if (!commands.containsKey(Symbol.OPERATION)){
				
				// тепер дивимось чи є в нас записане перше число в наш EnumMap, якщо ні , то записуєм
				if (!commands.containsKey(Symbol.FIRST_DIGIT)){
					commands.put(Symbol.FIRST_DIGIT, txtResult.getText());
				}
				
				// записуєм в енум мап тип операції
				commands.put(Symbol.OPERATION, operType);
				
				// якщо це вже другий раз нажата операція	
				// і якщо друге число не записане в наш мап, то записуєм робимо обчислення та звільнаєм в мапі місце під друге число
			} else if (!commands.containsKey(Symbol.SECOND_DIGIT)){
				commands.put(Symbol.SECOND_DIGIT, txtResult.getText());
				doCalc();
				commands.put(Symbol.OPERATION, operType);
				commands.remove(Symbol.SECOND_DIGIT);
			}
			
			lastAction = ActionType.OPERATION;
			
			break;			
		}
		
		case R.id.btnClean:{// кнопка очистити поле введення
			txtResult.setText("0");
			// також очищаємо нашу мапу повністю
			commands.clear();
			lastAction = ActionType.CLEAR;
			break;
		}
			
		case R.id.btnEqulas:{// кнопка підрахунку результату
			if (lastAction == ActionType.CALCULATION) {return;}
			if (commands.containsKey(Symbol.FIRST_DIGIT) && commands.containsKey(Symbol.OPERATION)){// якщо ввели перше число і операцію
				                                                                                    // підраховуємо результат
				commands.put(Symbol.SECOND_DIGIT, txtResult.getText());
				doCalc();
				commands.clear();;
			}
			
			lastAction = ActionType.CALCULATION;
			break;
		}
		
		case R.id.btnVigule:{// кнопка для введення десяткового числа
			// Якщо спочатку було введено перше число, потім операція, потім зразу кома, то ми спочатку в друге число вводимо 0 потім "," аж потім цифри
			if (commands.containsKey(Symbol.FIRST_DIGIT) && getDouble(txtResult.getText().toString()) == getDouble(commands.get(Symbol.FIRST_DIGIT))){
				txtResult.setText("0"+v.getContentDescription().toString());
			}
			
			// причому якщо кома вже раз була нажата і виведена, то при другому нажиманні виводити не будемо
			if (!txtResult.getText().toString().contains(",")){
				txtResult.setText(txtResult.getText()+v.getContentDescription().toString());
			} 
			
			lastAction = ActionType.COMMA;
			break;
		}		
		
		case R.id.btnBack:{// кнопка видалення останньої цифри
			txtResult.setText(txtResult.getText().delete(txtResult.getText().length()-1, txtResult.getText().length()));
			
			// якщо видалено всі символи мусимо записати 0 в txtResult
			if (txtResult.getText().toString().trim().length() == 0){
				txtResult.setText("0");
			}
			
			lastAction = ActionType.DELETE;
			break;
		}
				
		// всі інші кнопки, тобто цифри
		default:{
			
			// перевіряєм чи в txtResult 0, потім чи існує вже перше число, тобто була натиснути кнопка операції, якщо так, то починаєм наново вводити цифри
			if (txtResult.getText().toString().equals("0") 
					|| 
			   (commands.containsKey(Symbol.FIRST_DIGIT) && getDouble(txtResult.getText())==getDouble(commands.get(Symbol.FIRST_DIGIT))) 
			           ||
			   (lastAction == ActionType.CALCULATION)){
				      txtResult.setText(v.getContentDescription().toString());
			}
			
			else{ 			
			    txtResult.setText(txtResult.getText()+v.getContentDescription().toString());
			}
			
			lastAction = ActionType.DIGIT;
			break;
		}
	  }		
		
	}
	
	
	private void showResult(double result){
		
		// перевіряємо чи результат після операціїї ціле число(тобто якщо нема остатку ділення на 0), якщо так , то відсікаєм нулі
		if (result%1 == 0){
			txtResult.setText(String.valueOf((int)result));// відсікаємо нулі
		} else{
			txtResult.setText((String.valueOf(result)).replace(".", ","));// не відсікаємо нулі
		}
	}


	private void doCalc() {
		
		// в змінну operTypeTemp записуємо перший знак операції натиснутий на калькуляторі
		OperationType operTypeTemp = (OperationType)commands.get(Symbol.OPERATION);
		
		double result = 0;
		try{
		result = calc(operTypeTemp, getDouble(commands.get(Symbol.FIRST_DIGIT)), getDouble(commands.get(Symbol.SECOND_DIGIT)));
		} catch (DivisionByZeroException e){
			Toast.makeText(this, R.string.division_zero, Toast.LENGTH_LONG).show();
			return;
		
		}
		// перевіряємо чи результат після операціїї ціле число(тобто якщо нема остатку ділення на 0), якщо так , то відсікаєм нулі
		showResult(result);
		
		commands.put(Symbol.FIRST_DIGIT, result);// записуємо результат обчислень в перше число, щоб можна було далі проводити обчислення	
		
	}
	
	
	private Double calc(OperationType operType, double x,
			double y) {
		
		switch(operType){
		case ADD:
			context.setStrategy(add);
			forCalc.put(OperationType.ADD, Context.execute(x, x));
		case DIVIDE:
			context.setStrategy(div);
			forCalc.put(OperationType.DIVIDE, Context.execute(x, x));
		case MULTIPLY:
			context.setStrategy(mul);
			forCalc.put(OperationType.MULTIPLY, Context.execute(x, x));
		case SUBTRACT:
			context.setStrategy(sub);			
			forCalc.put(OperationType.SUBTRACT, Context.execute(x, x));
		}
		return forCalc.get(operType);
	}


	private double getDouble(Object value){
		
		double result = 0;
		try{ 
		result = Double.valueOf(value.toString().replace(",", ".")).doubleValue(); // тут заміняєм , на . в числі набраному в калькуляторі
		                                                                           // і приводим до примітивного типу double 
		} catch (Exception e){
			Log.e(TAG, e+"");
			result = 0;
		}
		return result;
	}

	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.calculator, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()){
		
		case R.id.Classic:
			currentTheme = R.style.AppTheme;
			break;
			
		case R.id.Modern:
			currentTheme = R.style.CalculationTheme;
			break;
			
		case R.id.AboutProgram:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
			
		default: return super.onOptionsItemSelected(item);
		
		}
		
		// якщо був вибраний один із пунктів підменю "Теми", тоді закінчуємо дію поточного Activity, для того щоб вступили в дію зміни, а саме помінялася тема 
		// Activity згідно із змінною currentTheme
		finish();
		startActivity(new Intent(this, getClass()));
	
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	
	
	
}
