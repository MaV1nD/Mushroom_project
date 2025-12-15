FROM python:3.9-slim

WORKDIR /app

# Установка системных библиотек (иногда нужны для Pillow/TensorFlow)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копируем код И МОДЕЛЬ с метками
COPY main.py .
COPY mushroom_model.keras .
COPY labels.txt .

CMD ["python", "main.py"]