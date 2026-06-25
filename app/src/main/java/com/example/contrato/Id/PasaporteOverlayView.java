package com.example.contrato.Id;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PasaporteOverlayView extends View {

    // Pintura utilizada para dibujar el borde
    private Paint borderPaint;

    // Rectángulo que representa el área de la identificación
    private RectF cardRect;

    public PasaporteOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Configuración del borde
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);      // Color blanco
        borderPaint.setStyle(Paint.Style.STROKE); // Solo borde
        borderPaint.setStrokeWidth(8);          // Grosor del borde
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Ancho del rectángulo = 85% del ancho de la pantalla
        float ancho = getWidth() * 0.85f;

        // Altura proporcional a una tarjeta de identificación
        float alto = ancho * (9f / 13f);

        // Calcula la posición para centrar el rectángulo
        float izquierda = (getWidth() - ancho) / 2;
        float arriba = (getHeight() - alto) / 2;

        // Crea el rectángulo
        cardRect = new RectF(
                izquierda,
                arriba,
                izquierda + ancho,
                arriba + alto
        );

        // Dibuja el rectángulo con esquinas redondeadas
        canvas.drawRoundRect(cardRect, 20, 20, borderPaint);
    }

    /**
     * Devuelve las coordenadas del área donde debe ir la identificación.
     * Estas coordenadas se pueden usar después para recortar la foto.
     */
    public RectF getCardRect() {
        return cardRect;
    }
}
