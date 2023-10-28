package com.example.sapper;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Random random = new Random();
    TextView[][] cells; // Объявление двумерного массива TextView для ячеек
    TextView mines_count; // Объявление TextView для отображения количества мин

    private Mines[][] mines;

    final int MINESCONST = 30; // Константа для общего количества мин
    int minesCurrent = MINESCONST; // Текущее количество мин
    final int WIDTH = 9; // Ширина игрового поля
    final int HEIGHT = 17; // Высота игрового поля
    boolean is_first_click = false; // Нажание на первую ячейку
    public boolean end = false; // Конец игры
    public int x, y; // Координаты нажатой ячейки
    public void generation() {
        int mines_generation = MINESCONST; // Число мин для генерации
        int bomb_x, bomb_y;
        while (mines_generation > 0) {
            bomb_x = random.nextInt(WIDTH);
            bomb_y = random.nextInt(HEIGHT);
            if (!this.mines[bomb_y][bomb_x].is_bomb && (Math.abs(this.x - bomb_x) > 1 || Math.abs(this.y - bomb_y) > 1)) {
                this.mines[bomb_y][bomb_x].is_bomb = true;
                //cells[bomb_y][bomb_x].setText("M");
                mines_generation--;
            }
        }
        for(int i=0; i<HEIGHT;i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (mines[i][j].is_bomb) {
                    for (int k = -1; k < 2; k++) {
                        for (int l = -1; l < 2; l++) {
                            if (k + i >= 0 && j + l >= 0 && k + i < HEIGHT && j + l < WIDTH)
                                mines[i + k][j + l].mines_count += 1;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mines_count = findViewById(R.id.mines); // Находим TextView для отображения количества мин

        mines_count.setText(""+ minesCurrent + " / " + MINESCONST); // Устанавливаем текст для отображения количества мин

        generate(); // Вызываем функцию для генерации игрового поля
    }

    public void generate() {
        GridLayout layout = findViewById(R.id.grid); // Находим GridLayout для размещения ячеек
        layout.removeAllViews(); // Очищаем GridLayout от предыдущих элементов
        layout.setColumnCount(WIDTH); // Устанавливаем количество столбцов в GridLayout
        cells = new TextView[HEIGHT][WIDTH]; // Создаем двумерный массив для ячеек
        mines = new Mines[HEIGHT][WIDTH]; // Создаем двумерный массив для мин
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                mines[i][j] = new Mines(); // Инициализация объекта Mines
            }
        }

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        // Создаем ячейки и добавляем их в GridLayout
        for(int i=0; i<HEIGHT;i++){
            for(int j=0; j<WIDTH;j++){
                cells[i][j] = (TextView) inflater.inflate(R.layout.cell, layout, false);
            }
        }

        // Настраиваем обработчики нажатия на ячейки
        for(int i=0; i<HEIGHT;i++){
            for(int j=0; j<WIDTH;j++){
                // Получение координат нажания
                cells[i][j].setTag(""+j+HEIGHT*i);
                int finalJ = j;
                int finalI = i;
                cells[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (end) return;
                        x = finalJ;
                        y = finalI;

                        if (MainActivity.this.mines[MainActivity.this.y][MainActivity.this.x].is_bomb) {
                            MainActivity.this.end = true;
                            for(int i=0; i<HEIGHT;i++) {
                                for (int j = 0; j < WIDTH; j++) {
                                    if (MainActivity.this.mines[i][j].is_bomb && !MainActivity.this.mines[i][j].is_flag)
                                        cells[i][j].setBackgroundResource(R.drawable.mine_background_explosion); // Устанавливаем фон взрывe
                                    else if (MainActivity.this.mines[i][j].is_bomb && MainActivity.this.mines[i][j].is_flag)
                                        cells[i][j].setBackgroundResource(R.drawable.mine_background_not_explosion); // Устанавливаем фон с флажком и миной
                                }
                            }
                            v.setBackgroundResource(R.drawable.mine_background_explosion_2); // Устанавливаем фон взрыва при клике
                            Toast.makeText(getApplicationContext(), "BOOM!!!", Toast.LENGTH_LONG).show(); // Выводим сообщение о взрыве
                        } else {
                            MainActivity.this.mines[MainActivity.this.y][MainActivity.this.x].is_open = true;
                            // Обновляем интерфейс для текущей ячейки
                            cells[y][x].setText(mines[y][x].mines_count != 0 ? String.valueOf(mines[y][x].mines_count) : "");
                            cells[y][x].setBackgroundResource(R.drawable.mine_backgroung_open);

                            if (!is_first_click) {
                                is_first_click = true;
                                generation();
                            }
                        }
                        if (MainActivity.this.mines[MainActivity.this.y][MainActivity.this.x].mines_count==0)
                            openAdjacentCells(y, x);
                    }

                    // Внутри вашего обработчика onClick
                    private void openAdjacentCells(int row, int col) {
                        // Обходим соседние ячейки (лево, право, верх, низ)
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int newRow = row + i;
                                int newCol = col + j;
                                if (newRow >= 0 && newRow < HEIGHT && newCol >= 0 && newCol < WIDTH
                                        && !mines[newRow][newCol].is_open && !mines[newRow][newCol].is_flag
                                        && !mines[newRow][newCol].is_bomb) {
                                    if (mines[newRow][newCol].mines_count == 0) {
                                        // Установка is_open = true и обновление интерфейса
                                        mines[newRow][newCol].is_open = true;
                                        if (mines[newRow][newCol].mines_count!=0) cells[newRow][newCol].setText(String.valueOf(mines[newRow][newCol].mines_count));
                                        cells[newRow][newCol].setBackgroundResource(R.drawable.mine_backgroung_open);
                                        // Рекурсивное открытие соседей
                                        openAdjacentCells(newRow, newCol);
                                    } else {
                                        // Открывается только если mines_count равен 0
                                        mines[newRow][newCol].is_open = true;
                                        if (mines[newRow][newCol].mines_count!=0) cells[newRow][newCol].setText(String.valueOf(mines[newRow][newCol].mines_count));
                                        cells[newRow][newCol].setBackgroundResource(R.drawable.mine_backgroung_open);
                                    }
                                }
                            }
                        }
                    }

                });
                cells[i][j].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (end) return true;
                        if (!is_first_click) {
                            v.setBackgroundResource(R.drawable.mine_backgroung_open); // Устанавливаем фон ячейки при клике
                            is_first_click = true;
                            generation();
                        } else {
                                // Отметка флажком
                                if (!mines[finalI][finalJ].is_flag && !mines[finalI][finalJ].is_open) {
                                    v.setBackgroundResource(R.drawable.mine_backgroung_flag);
                                    mines[finalI][finalJ].is_flag = true;
                                    minesCurrent--; // Уменьшаем количество мин
                                } else if (mines[finalI][finalJ].is_flag) {
                                    // Снятие флажка
                                    v.setBackgroundResource(R.drawable.mine_background_close);
                                    mines[finalI][finalJ].is_flag = false;
                                    minesCurrent++; // Увеличиваем количество мин
                                }
                                mines_count.setText(""+ minesCurrent + " / " + MINESCONST); // Обновляем отображение количества мин
                                int count = 0;
                                for(int i=0; i<HEIGHT;i++){
                                    for(int j=0; j<WIDTH;j++){
                                        if (mines[i][j].is_flag && mines[i][j].is_bomb) count++;

                                    }
                                }
                                if (count == MINESCONST) {
                                    end = true;
                                    Toast.makeText(getApplicationContext(), "WIN!!!", Toast.LENGTH_LONG).show(); // Выводим сообщение о победе
                                } else count = 0;
                        }
                        return true;
                    }
                });


                layout.addView(cells[i][j]); // Добавляем ячейку в GridLayout
            }
        }
    }
}
