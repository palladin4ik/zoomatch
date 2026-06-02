from django.core.exceptions import ValidationError
from django.conf import settings


def validate_file_size(file):
    max_mb = getattr(settings, 'MAX_UPLOAD_SIZE_MB', 10)
    if file.size > max_mb * 1024 ** 2:
        raise ValidationError(f'Файл не должен превышать {max_mb} МБ.')


def validate_image_type(file):
    allowed = getattr(settings, 'ALLOWED_IMAGE_TYPES',
                      ['image/jpeg', 'image/png', 'image/webp'])
    content_type = getattr(file, 'content_type', None)
    if content_type not in allowed:
        raise ValidationError(
            f'Недопустимый тип файла. Разрешены: {", ".join(allowed)}'
        )


def validate_media_type(file):
    allowed = getattr(settings, 'ALLOWED_MEDIA_TYPES', [])
    content_type = getattr(file, 'content_type', None)
    if content_type not in allowed:
        raise ValidationError(
            f'Недопустимый тип файла. Разрешены: {", ".join(allowed)}'
        )


def validate_pdf_type(file):
    content_type = getattr(file, 'content_type', None)

    if content_type != 'application/pdf':
        raise ValidationError('Недопустимый тип файла. Разрешен только PDF')
