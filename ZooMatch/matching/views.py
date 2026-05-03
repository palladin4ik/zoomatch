from rest_framework import viewsets, permissions, mixins, status
from rest_framework.response import Response
from django.db.models import Q, F

from .serializers import MatchSerializer
from .models import Match, Rejection


class MatchViewSet(mixins.CreateModelMixin, mixins.ListModelMixin,
                   mixins.UpdateModelMixin, viewsets.GenericViewSet):
    serializer_class = MatchSerializer
    permission_classes = [permissions.IsAuthenticated]
    http_method_names = ['get', 'post', 'patch']

    def perform_create(self, serializer):
        pet_from = serializer.validated_data['pet_from']
        pet_to = serializer.validated_data['pet_to']
        
        reverse_match = Match.objects.filter(pet_from=pet_to,
                                             pet_to=pet_from).first()
        if reverse_match:
            reverse_match.status = Match.Status.ACCEPTED
            reverse_match.save()

            return
        
        serializer.save()

    def get_queryset(self):
        user = self.request.user
        match_type = self.request.query_params.get('type')

        if match_type == 'sent':
            qs = Match.objects.filter(
                pet_from__owner=user
            )
        elif match_type == 'received':
            qs = Match.objects.filter(
                pet_to__owner=user
            )
        else:
            qs = Match.objects.filter(
                Q(pet_from__owner=user) |
                Q(pet_to__owner=user)
            )
        
        return qs.select_related('pet_from', 'pet_to')
    
    def partial_update(self, request, *args, **kwargs):
        match = self.get_object()

        if match.pet_to.owner != request.user:
            return Response(status=status.HTTP_403_FORBIDDEN)
        
        new_status = request.data.get('status')
        if new_status == Match.Status.REJECTED:
            pet_from = match.pet_from
            pet_to = match.pet_to

            rejection, created = Rejection.objects.get_or_create(
                pet_from=pet_from,
                pet_to=pet_to
            )

            if not created:
                rejection.count = F('count') + 1
                rejection.save()

        serializer = self.get_serializer(match, data={'status': new_status},
                                         partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        return Response(serializer.data, status=status.HTTP_200_OK)
