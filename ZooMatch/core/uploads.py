import os
import uuid
from datetime import datetime


def build_path(directory, filename):
    ext = os.path.splitext(filename)[1].lower()
    unique_name = f'{uuid.uuid4().hex}{ext}'
    now = datetime.now()

    return os.path.join(directory, str(now.year), str(now.month), unique_name)


def user_avatar_path(instance, filename):
    # media/users/avatars/2025/5/abc123.jpg
    return build_path('users/avatars', filename)


def pet_avatar_path(instance, filename):
    # media/pets/avatars/2025/5/abc123.jpg
    return build_path('pets/avatars', filename)


def pet_document_path(instance, filename):
    # media/pets/documents/2025/5/abc123.pdf
    return build_path('pets/documents', filename)


def comment_media_path(instance, filename):
    return build_path('comments/media', filename)


def message_media_path(instance, filename):
    # Разбивка по match_id для удобства приватного доступа в будущем
    match_id = getattr(instance, 'match_id', 'unknown')
    return build_path(f'chats/{match_id}', filename)
