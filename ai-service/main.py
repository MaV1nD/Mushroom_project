from fastapi import FastAPI, UploadFile, File
import uvicorn
import numpy as np
from io import BytesIO
from PIL import Image
import tensorflow as tf
import os

app = FastAPI()

model = None
class_names = []

@app.on_event("startup")
async def startup_event():
    global model, class_names
    print("--- ЗАПУСК AI СЕРВИСА ---")
    
    # 1. Проверяем файлы
    if not os.path.exists('mushroom_model.keras'):
        print("ОШИБКА: Файл mushroom_model.keras не найден!")
    if not os.path.exists('labels.txt'):
        print("ОШИБКА: Файл labels.txt не найден!")

    # 2. Загружаем метки
    with open('labels.txt', 'r') as f:
        class_names = [line.strip() for line in f.readlines() if line.strip()]
    
    print(f"DEBUG: Загружено {len(class_names)} классов из labels.txt")
    print(f"DEBUG: Первые 3 класса: {class_names[:3]}")
    
    # 3. Загружаем модель
    print("Загрузка модели TensorFlow...")
    model = tf.keras.models.load_model('mushroom_model.keras')
    print("Модель успешно загружена.")

def preprocess_image(image_bytes):
    # Открываем картинку
    img = Image.open(BytesIO(image_bytes)).convert('RGB')
    # Ресайз под размер, на котором училась модель (224x224)
    img = img.resize((224, 224))
    img_array = tf.keras.utils.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0) 
    return img_array

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    print(f"Получен запрос. Файл: {file.filename}")
    
    image_bytes = await file.read()
    
    if len(image_bytes) == 0:
        print("ОШИБКА: Пришел пустой файл!")
        return {"error": "Empty file"}

    img_array = preprocess_image(image_bytes)
    
    # Предсказание
    predictions = model.predict(img_array)
    score = tf.nn.softmax(predictions[0])
    
    predicted_class_index = np.argmax(score)
    predicted_class_name = class_names[predicted_class_index]
    confidence = 100 * np.max(score)
    
    print(f"DEBUG: Предсказание: Индекс={predicted_class_index}, Имя='{predicted_class_name}', Точность={confidence:.2f}%")

    return {
        "class": predicted_class_name, 
        "confidence": float(confidence)
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)