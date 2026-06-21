from django.db import models
from django.contrib.auth import get_user_model

from core.uploads import (pet_avatar_path, pet_document_path,
                          comment_media_path)
from core.validators import (validate_file_size, validate_image_type,
                             validate_media_type, validate_pdf_type)


User = get_user_model()


class AnimalType(models.Model):
    name = models.CharField(max_length=50, unique=True)


class Breed(models.Model):
    name = models.CharField(max_length=100, unique=True)
    animal_type = models.ForeignKey(
        AnimalType,
        on_delete=models.CASCADE,
        related_name='breeds',
    )


class Tag(models.Model):
    tag = models.CharField(max_length=50, unique=True)


class Pet(models.Model):

    class ModerationStatus(models.TextChoices):
        PENDING = 'pending', 'На модерации'
        APPROVED = 'approved', 'Одобрено'
        REJECTED = 'rejected', 'Отклонено'

    name = models.CharField(max_length=150)
    animal_type = models.ForeignKey(
        AnimalType,
        on_delete=models.SET_NULL,
        null=True,
        related_name='pets',
    )
    breed = models.ForeignKey(
        Breed,
        on_delete=models.SET_NULL,
        null=True,
        related_name='pets',
    )
    is_male = models.BooleanField()
    age = models.PositiveSmallIntegerField()
    owner = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='pets'
    )
    avatar = models.ImageField(
        upload_to=pet_avatar_path,
        blank=True, null=True,
        validators=[validate_file_size, validate_image_type]
    )
    location = models.CharField(max_length=100)
    has_pedigree = models.BooleanField(default=False)
    pedigree_documents = models.FileField(
        upload_to=pet_document_path,
        blank=True, null=True,
        validators=[validate_file_size, validate_pdf_type]
    )
    awards = models.TextField(blank=True, null=True)
    tags = models.ManyToManyField(
        Tag,
        related_name='pets',
        blank=True
        )
    description = models.TextField(blank=True, null=True)
    is_active = models.BooleanField(default=False, db_index=True)
    moderation_status = models.CharField(
        max_length=20,
        choices=ModerationStatus.choices,
        default=ModerationStatus.APPROVED,
        db_index=True
    )

    last_mating_date = models.DateField(blank=True, null=True)
    mating_count = models.PositiveSmallIntegerField(default=0)

    class Meta:
        indexes = [
            models.Index(fields=['animal_type', 'breed']),
            models.Index(fields=['animal_type', 'is_active']),
            models.Index(fields=['location', 'animal_type']),
            models.Index(fields=['is_active', 'moderation_status']),
        ]


class PetInfo(models.Model):
    pet = models.OneToOneField(
        Pet,
        on_delete=models.CASCADE,
        related_name='pet_info',
    )
    watchers = models.PositiveIntegerField()
    likes = models.PositiveIntegerField()


# Подумать
class Comment(models.Model):
    text = models.TextField()  # new
    media = models.FileField(
        upload_to=comment_media_path,
        blank=True, null=True,
        validators=[validate_file_size, validate_media_type]
    )  # было content
    likes = models.PositiveIntegerField(default=0)
    author = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='comments',
    )
    pet_info_card = models.ForeignKey(
        PetInfo,
        on_delete=models.CASCADE,
        related_name='comments',
    )
    date_create = models.DateTimeField(auto_now_add=True)
