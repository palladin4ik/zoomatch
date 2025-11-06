from django.db import models
from django.contrib.auth import get_user_model
from django.utils import timezone


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
        related_name='pets',
    )
    breed = models.ForeignKey(
        Breed,
        on_delete=models.SET_NULL,
        related_name='pets',
    )
    is_male = models.BooleanField()
    age = models.PositiveSmallIntegerField()
    owner = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='pets'
    )
    avatar = models.FilePathField(blank=True, null=True)
    location = models.TextField()
    has_pedigree = models.BooleanField(default=False)
    pedigree_documents = models.FilePathField()
    awards = models.TextField(blank=True, null=True)
    description = models.TextField(blank=True, null=True)
    is_active = models.BooleanField(default=False)


class PetInfo(models.Model):
    pet = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='pet_info',
    )
    watchers = models.PositiveIntegerField()
    likes = models.PositiveIntegerField()


class Comment(models.Model):
    content = models.TextField()
    likes = models.PositiveIntegerField(default=0)
    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='comments',
    )
    pet_info_card = models.ForeignKey(
        PetInfo,
        on_delete=models.CASCADE,
        related_name='comments',
    )
    date_create = models.DateTimeField(default=timezone.now)
