import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import numpy as np
import os
import matplotlib.pyplot as plt

# ==========================================
# CONFIGURACIÓN
# ==========================================
IMG_SIZE = 224
BATCH_SIZE = 32
EPOCHS = 20
LEARNING_RATE = 0.001
DATASET_PATH = 'dataset' # Estructura: dataset/train y dataset/val

# Definición de clases según DefectClassifier.kt
CLASS_NAMES = [
    "agujero_ok",
    "agujero_defectuoso",
    "avellanado_ok",
    "avellanado_defectuoso",
    "sin_rayadura",
    "rayadura_leve",
    "rayadura_severa",
    "sin_deformacion",
    "deformado",
    "alodine_ok",
    "alodine_ausente",
    "alodine_irregular"
]
NUM_CLASSES = len(CLASS_NAMES)

def train_model():
    print("🚀 Iniciando configuración de entrenamiento...")
    
    # 1. Pipeline de Data Augmentation
    data_augmentation = keras.Sequential([
        layers.RandomFlip("horizontal_and_vertical"),
        layers.RandomRotation(0.2),
        layers.RandomZoom(0.2),
        layers.RandomContrast(0.2),
        layers.RandomBrightness(0.2),
    ])

    # 2. Cargar Datasets
    # Nota: Asegúrate de tener las carpetas creadas aunque estén vacías inicialmente
    if not os.path.exists(DATASET_PATH):
        print(f"❌ Error: No se encuentra la carpeta '{DATASET_PATH}'")
        print("Por favor crea la estructura de carpetas y agrega imágenes.")
        return

    print("📸 Cargando dataset de entrenamiento...")
    train_ds = tf.keras.utils.image_dataset_from_directory(
        os.path.join(DATASET_PATH, 'train'),
        validation_split=None,
        image_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        label_mode='categorical'
    )

    print("📸 Cargando dataset de validación...")
    val_ds = tf.keras.utils.image_dataset_from_directory(
        os.path.join(DATASET_PATH, 'val'),
        validation_split=None,
        image_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        label_mode='categorical'
    )

    # Autotune para optimizar carga
    AUTOTUNE = tf.data.AUTOTUNE
    train_ds = train_ds.cache().shuffle(1000).prefetch(buffer_size=AUTOTUNE)
    val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)

    # 3. Construir Modelo (MobileNetV3-Small)
    # Optimizado para dispositivos móviles (Android)
    print("🧠 Construyendo modelo MobileNetV3Large...")
    
    base_model = tf.keras.applications.MobileNetV3Small(
        input_shape=(IMG_SIZE, IMG_SIZE, 3),
        include_top=False,
        weights='imagenet',
        minimalistic=False
    )

    base_model.trainable = False # Congelar capas base

    inputs = keras.Input(shape=(IMG_SIZE, IMG_SIZE, 3))
    x = data_augmentation(inputs)
    # Pre-procesamiento específico para MobileNetV3
    x = tf.keras.applications.mobilenet_v3.preprocess_input(x)
    x = base_model(x, training=False)
    x = layers.GlobalAveragePooling2D()(x)
    x = layers.Dropout(0.3)(x)
    x = layers.Dense(128, activation='relu')(x)
    x = layers.Dropout(0.2)(x)
    outputs = layers.Dense(NUM_CLASSES, activation='softmax')(x)

    model = keras.Model(inputs, outputs)

    # 4. Compilar Modelo
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=LEARNING_RATE),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    model.summary()

    # 5. Entrenar (Transfer Learning)
    print("🏋️‍♂️ Iniciando entrenamiento (Fase 1 - Transfer Learning)...")
    history = model.fit(
        train_ds,
        epochs=EPOCHS,
        validation_data=val_ds
    )

    # 6. Fine-Tuning
    print("🔧 Iniciando Fine-Tuning...")
    base_model.trainable = True
    
    # Congelar las primeras capas, entrenar solo las últimas
    # Ajustar según la profundidad del modelo
    fine_tune_at = 100
    for layer in base_model.layers[:fine_tune_at]:
        layer.trainable = False

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=LEARNING_RATE / 10), # LR más bajo
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    history_fine = model.fit(
        train_ds,
        epochs=10,
        validation_data=val_ds
    )

    # 7. Convertir a TensorFlow Lite
    print("📱 Convirtiendo a TFLite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Optimizaciones estándar
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Opcional: Cuantización Float16 para GPU
    converter.target_spec.supported_types = [tf.float16]
    
    tflite_model = converter.convert()

    # 8. Guardar Modelo
    output_path = 'defect_classifier.tflite'
    with open(output_path, 'wb') as f:
        f.write(tflite_model)

    print(f"✅ Modelo guardado exitosamente: {output_path}")
    print(f"📦 Tamaño: {len(tflite_model) / 1024 / 1024:.2f} MB")

    # Guardar métricas
    plot_history(history, history_fine)

def plot_history(history, history_fine):
    acc = history.history['accuracy'] + history_fine.history['accuracy']
    val_acc = history.history['val_accuracy'] + history_fine.history['val_accuracy']
    loss = history.history['loss'] + history_fine.history['loss']
    val_loss = history.history['val_loss'] + history_fine.history['val_loss']

    plt.figure(figsize=(8, 8))
    plt.subplot(2, 1, 1)
    plt.plot(acc, label='Training Accuracy')
    plt.plot(val_acc, label='Validation Accuracy')
    plt.ylim([0, 1])
    plt.plot([EPOCHS-1, EPOCHS-1], plt.ylim(), label='Start Fine Tuning')
    plt.legend(loc='lower right')
    plt.title('Training and Validation Accuracy')

    plt.subplot(2, 1, 2)
    plt.plot(loss, label='Training Loss')
    plt.plot(val_loss, label='Validation Loss')
    plt.ylim([0, 1.0])
    plt.plot([EPOCHS-1, EPOCHS-1], plt.ylim(), label='Start Fine Tuning')
    plt.legend(loc='upper right')
    plt.title('Training and Validation Loss')
    plt.xlabel('epoch')
    plt.savefig('training_results.png')
    print("📊 Gráfica de entrenamiento guardada como training_results.png")

if __name__ == "__main__":
    train_model()
