from django.db import models
from django.contrib.auth import get_user_model


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
    avatar = models.TextField(blank=True, null=True)
    location = models.CharField(max_length=100)
    has_pedigree = models.BooleanField(default=False)
    pedigree_documents = models.TextField(blank=True, null=True)
    awards = models.TextField(blank=True, null=True)
    tags = models.ManyToManyField(
        Tag,
        related_name='pets',
        blank=True
        )
    description = models.TextField(blank=True, null=True)
    is_active = models.BooleanField(default=False, db_index=True)

    class Meta:
        indexes = [
            models.Index(fields=['animal_type', 'breed']),
            models.Index(fields=['animal_type', 'is_active']),
            models.Index(fields=['location', 'animal_type']),
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
    media = models.TextField(blank=True, null=True)  # было content
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
