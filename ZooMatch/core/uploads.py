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
    sender_id = getattr(instance, 'sender_id', None)
    receiver_id = getattr(instance, 'receiver_id', None)
    if sender_id and receiver_id:
        pair = sorted([sender_id, receiver_id])
        return build_path(f'chats/{pair[0]}_{pair[1]}', filename)
    return build_path('chats/misc', filename)
