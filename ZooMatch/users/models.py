from django.contrib.auth.models import (AbstractBaseUser,
                                        BaseUserManager, PermissionsMixin)
from django.db import models
from django.utils import timezone

from core.uploads import user_avatar_path
from core.validators import validate_file_size, validate_image_type


class CustomUserManager(BaseUserManager):
    def create_user(self, email, password=None, **extra_fields):
        if not email:
            raise ValueError('Email обязателен')
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email, password, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        return self.create_user(email, password, **extra_fields)


class User(AbstractBaseUser, PermissionsMixin):

    class Role(models.IntegerChoices):
        USER = 0, 'User'
        BREEDER = 1, 'Breeder'
        ADMIN = 2, 'Admin'

    firstname = models.CharField(max_length=50)  # было name
    lastname = models.CharField(max_length=50)  # new
    email = models.EmailField(unique=True)
    password = models.CharField(max_length=128)
    avatar = models.ImageField(
        upload_to=user_avatar_path,
        blank=True, null=True,
        validators=[validate_file_size, validate_image_type]
    )
    location = models.CharField(max_length=100,
                                blank=True, null=True)
    description = models.TextField(blank=True, null=True)  # было status

    organization = models.CharField(max_length=150,
                                    blank=True, null=True)  # new

    phone_number = models.CharField(
        max_length=20,
        unique=True,
        blank=True, null=True)

    role = models.PositiveSmallIntegerField(
        choices=Role.choices,
        default=Role.USER,
        db_index=True
        )

    last_seen = models.DateTimeField(default=timezone.now, db_index=True)

    is_active = models.BooleanField(default=True, db_index=True)
    is_staff = models.BooleanField(default=False)

    objects = CustomUserManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['firstname']

    def __str__(self):
        return self.email
